# Inventory Outbox Flow

## 1. Purpose

本文档面向研发、测试、运维，说明 `Inventory` 审计 outbox 的完整流程。  
本文档覆盖主业务命令幂等、审计延后写、outbox 重试、死信、人工重放、批量重放任务。  
本文档用于帮助人理解链路，不作为索引入口，不补充到 `docs/AGENT.md` 或其他文档索引。

## 2. Scope

当前范围：

- `reserveStock`
- `releaseReservedStock`
- `deductReservedStock`
- 审计日志延后写
- `InventoryAuditOutbox` 自动重试
- `InventoryAuditDeadLetter` 死信处理
- 单条人工重放
- 批量重放任务

不在当前范围：

- `Order` 域 outbox
- `Payment` 域回调幂等
- 通用消息队列语义

## 3. Core Objects

- `InventoryReservation`
- `InventoryLedger`
- `InventoryAuditLog`
- `InventoryAuditOutbox`
- `InventoryAuditDeadLetter`
- `InventoryAuditReplayTask`
- `InventoryAuditReplayTaskItem`

关键状态：

- `InventoryReservationStatus`: `CREATED`、`RESERVED`、`RELEASED`、`DEDUCTED`、`FAILED`
- `InventoryAuditOutboxStatus`: `NEW`、`PROCESSING`、`RETRYING`、`DEAD`
- `InventoryAuditReplayStatus`: `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`
- `InventoryAuditReplayTaskStatus`: `PENDING`、`RUNNING`、`PAUSED`、`SUCCEEDED`、`FAILED`、`CANCELED`

## 4. Design Summary

`Inventory` 的 outbox 不是业务动作 outbox，而是审计补写 outbox。  
库存主业务事务先保证库存和预占单正确提交，审计日志在事务提交后 best effort 写入。  
如果审计写失败，失败事实落到 `InventoryAuditOutbox`，后续由定时补偿任务重试。  
重试耗尽后进入 `InventoryAuditDeadLetter`，再由人工或批量任务回放。  
回放的目标不是再次执行库存预占、释放、扣减，而是补写丢失的审计日志，并更新死信追踪状态。

## 5. Main Flow

```mermaid
flowchart TD
    A[调用 InventoryCommandFacade] --> B[应用服务按 tenantId + orderNo 执行幂等入口]
    B --> C{是否已有 InventoryReservation}
    C -- 否 --> D[创建 CREATED 预占单]
    C -- 是 --> E{预占单状态}
    E -- CREATED --> F[继续完成未完成预占]
    E -- RESERVED/RELEASED/DEDUCTED/FAILED --> G[直接返回已有结果]

    D --> H[校验库存和请求]
    F --> H
    H -- 失败 --> I[预占单置 FAILED]
    H -- 成功 --> J[写库存数量]
    J --> K[预占单置 RESERVED / RELEASED / DEDUCTED]
    I --> L[主事务提交]
    K --> L

    L --> M[记录 InventoryLedger]
    L --> N[afterCommit 触发审计写入]
    N --> O{InventoryAuditLog 写入成功?}
    O -- 是 --> P[流程结束]
    O -- 否 --> Q[落库 InventoryAuditOutbox.status = NEW]
    Q --> R[等待定时重试]
```

## 6. Command Idempotency

### 6.1 Reserve

- 幂等键固定为 `tenantId + orderNo`
- 应用入口通过 `InventoryWriteRetrier` 包裹，冲突重试发生在事务外
- 若已存在预占单：
  - `CREATED`：继续补完
  - `RESERVED`：直接返回成功结果
  - `FAILED`：直接返回失败结果
  - `RELEASED`、`DEDUCTED`：直接返回已有终态结果
- `(tenantId, orderNo)` 唯一约束是最终兜底
- 创建预占单时若撞唯一键，回读已存在记录并直接收口为幂等返回

### 6.2 Release

- 幂等键固定为 `tenantId + orderNo`
- 若预占单不存在，返回失败结果，不修改库存
- 若预占单不是 `RESERVED`，直接返回当前状态，不重复回补库存
- 只有 `RESERVED -> RELEASED` 会真正执行库存回补和审计记录

### 6.3 Deduct

- 幂等键固定为 `tenantId + orderNo`
- 若预占单不存在，返回失败结果，不修改库存
- 若预占单不是 `RESERVED`，直接返回当前状态，不重复扣减库存
- 只有 `RESERVED -> DEDUCTED` 会真正执行库存扣减和审计记录

## 7. Audit Write And Outbox Fallback

```mermaid
flowchart TD
    A[主事务提交成功] --> B[TransactionSynchronization.afterCommit]
    B --> C[尝试写 InventoryAuditLog]
    C --> D{写入成功?}
    D -- 是 --> E[计 success 指标]
    D -- 否 --> F[计 fail 指标]
    F --> G[尝试写 InventoryAuditOutbox]
    G --> H{outbox 落库成功?}
    H -- 是 --> I[计 outbox persist success 指标]
    H -- 否 --> J[计 outbox persist fail 指标]
    J --> K[输出 ALERT 日志]
```

固定点：

- `InventoryLedger` 在主链路内同步写入
- `InventoryAuditLog` 在事务提交后写入，不回滚主业务
- `InventoryAuditOutbox` 保存原始审计动作和失败原因
- outbox 初始状态固定为 `NEW`

## 8. Retry And Dead Letter Flow

```mermaid
flowchart TD
    A[定时任务 InventoryAuditOutboxRetrier] --> B[释放 leaseUntil 已过期的 PROCESSING]
    B --> C[claim NEW/RETRYING 且 nextRetryAt <= now 的记录]
    C --> D[状态改为 PROCESSING]
    D --> E[补写 InventoryAuditLog]
    E --> F{补写成功?}
    F -- 是 --> G[按 owner + status CAS 删除 outbox]
    G --> H[计 retry success 指标]
    F -- 否 --> I[retryCount + 1]
    I --> J{超过 maxRetries?}
    J -- 否 --> K[指数退避 nextRetryAt]
    K --> L[按 owner + status CAS 置 RETRYING]
    L --> M[等待下一轮重试]
    J -- 是 --> N[按 owner + status CAS 置 DEAD]
    N --> O[写 InventoryAuditDeadLetter]
    O --> P[输出 ALERT exhausted 日志]
```

固定点：

- 多实例消费前必须 claim，不能先查后处理
- claim 写入 `processingOwner`、`leaseUntil`、`claimedAt`
- 重试结果提交必须走 owner + 状态 CAS
- 退避策略是指数退避，受 `baseDelaySeconds`、`maxDelaySeconds` 限制
- 超过最大重试次数后，不再自动重试，只进入死信

## 9. Dead Letter Replay Flow

```mermaid
flowchart TD
    A[人工调用 replayDeadLetter] --> B[读取 InventoryAuditDeadLetter]
    B --> C{replayStatus = SUCCEEDED?}
    C -- 是 --> D[直接返回 already-replayed]
    C -- 否 --> E[生成或使用 replayKey]
    E --> F[先 claim dead letter for replay]
    F --> G{claim 成功?}
    G -- 否 --> H[返回 dead-letter-not-claimable]
    G -- 是 --> I[新事务 replayClaimedDeadLetter]
    I --> J[补写原始 InventoryAuditLog]
    J --> K{成功?}
    K -- 是 --> L[死信置 SUCCEEDED]
    L --> M[写 AUDIT_REPLAY_SUCCEEDED 审计]
    M --> N[返回成功]
    K -- 否 --> O[死信置 FAILED]
    O --> P[写 AUDIT_REPLAY_FAILED 审计]
    P --> Q[返回失败]
    I --> R{事务直接异常?}
    R -- 是 --> S[补偿事务 compensateReplayTxFailure]
    S --> T[死信置 FAILED]
    T --> U[写 AUDIT_REPLAY_FAILED 审计]
```

固定点：

- 回放对象是死信，不是 outbox
- 回放语义是补写审计，不是重新执行库存业务
- `replayKey` 是幂等追踪键，单条重放和批量任务都必须写
- 回放前先 claim 死信，避免人工和后台任务并发重复处理
- 事务失败后还有单独补偿事务兜底，保证失败痕迹可见

## 10. Replay Task Flow

```mermaid
flowchart TD
    A[创建 replay task] --> B[写 InventoryAuditReplayTask]
    B --> C[批量写 InventoryAuditReplayTaskItem]
    C --> D[定时任务 Worker claim 可运行任务]
    D --> E[任务状态进入 RUNNING 并持有 lease]
    E --> F[分页拉取 PENDING task items]
    F --> G[逐条调用 replayDeadLetter]
    G --> H[写 item 成功/失败结果]
    H --> I[累计 processed/success/failed]
    I --> J{还有未处理项?}
    J -- 是 --> K[续租并进入下一批]
    J -- 否 --> L{failedCount = 0?}
    L -- 是 --> M[任务置 SUCCEEDED]
    L -- 否 --> N[任务置 FAILED]
```

任务规则：

- 任务本身只组织批量处理，单条语义仍复用 `replayDeadLetter`
- Worker 每轮都会续租，避免长任务被其他节点抢走
- item 逐条独立结算，单条失败不阻断整批推进
- 暂停任务时释放 `processingOwner` 和 `leaseUntil`
- 恢复任务时从 `PAUSED` 回到 `PENDING`

## 11. Full View

```mermaid
flowchart LR
    A[reserve/release/deduct] --> B[主事务提交]
    B --> C[afterCommit 写 InventoryAuditLog]
    C -->|成功| D[结束]
    C -->|失败| E[InventoryAuditOutbox NEW]
    E --> F[Retrier claim]
    F -->|补写成功| G[删除 outbox]
    F -->|补写失败且未超限| H[RETRYING]
    H --> F
    F -->|超过上限| I[DEAD + InventoryAuditDeadLetter]
    I --> J[人工 replayDeadLetter]
    I --> K[ReplayTaskWorker 批量回放]
    J --> L[补写原始审计]
    K --> L
    L -->|成功| M[DeadLetter SUCCEEDED + AUDIT_REPLAY_SUCCEEDED]
    L -->|失败| N[DeadLetter FAILED + AUDIT_REPLAY_FAILED]
```

## 12. Operations Checklist

- 看不到审计日志时，先查 `bacon_inventory_audit_outbox`
- 若 outbox 长时间停在 `PROCESSING`，优先检查 `lease_until` 是否回收
- 若 outbox 已是 `DEAD`，转查 `bacon_inventory_audit_dead_letter`
- 人工重放前先确认失败原因是否仍存在
- 批量重放时重点看任务维度的 `processedCount`、`successCount`、`failedCount`
- 若出现大量 `AUDIT_REPLAY_FAILED`，优先检查审计表写入能力，而不是库存主业务

## 13. Open Items

无
