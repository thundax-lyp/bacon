# STORAGE LAUNCH READINESS

## 1. Purpose

本文档定义 `Storage` 模块上线前必须完成的整改清单。  
目标是把当前实现中的 P0、P1、P2 风险收敛为可执行、可验收的工程任务，而不是停留在评审结论。

## 2. Scope

### 2.1 In Scope

- `bacon-storage-api`
- `bacon-storage-interfaces`
- `bacon-storage-application`
- `bacon-storage-domain`
- `bacon-storage-infra`
- `bacon-common-oss`
- `db/schema/storage.sql`

### 2.2 Out Of Scope

- 图片压缩
- 图片裁剪
- CDN 加速
- 文件内容安全扫描实现
- 对外业务域接口设计

## 3. Bounded Context

### 3.1 Storage

- `Storage` 拥有对象主数据、引用关系、分片上传会话和访问端点派生能力
- `Storage` 负责底层对象上传、删除、分片完成和取消
- `Storage` 不负责前端业务语义接口

### 3.2 Launch Readiness

- `P0` 表示阻断上线的问题，未完成不得上线
- `P1` 表示上线后高概率引发生产事故或高运维成本的问题，必须在正式流量前完成
- `P2` 表示提升稳定性、可观测性和治理效率的问题，应在首个稳定版本内完成

## 4. Global Constraints

- 所有 `P0` 项完成并通过验收前，`Storage` 不得以微服务模式对外承载正式上传流量
- 所有涉及数据一致性的整改必须同时覆盖 `LOCAL_FILE` 和 `OSS`
- 所有涉及接口契约的整改必须补自动化测试
- 所有涉及数据库字段或表结构的整改必须同步 `db/schema/storage.sql`、`STORAGE-REQUIREMENTS.md`、`STORAGE-DATABASE-DESIGN.md`

## 5. Functional Requirements

### 5.1 P0 Blocking Items

#### P0-1 Remote Upload Path Alignment

- `StoredObjectFacadeRemoteImpl.uploadObject` 固定调用正确的 provider 路径
- 普通上传、分片初始化、分片上传、分片完成、分片取消五条远程链路都必须逐条做路径契约测试
- 验收标准：
  - `micro` 模式下普通上传返回 `200`
  - 不允许出现 provider 路径和 remote facade URI 不一致

#### P0-2 Multipart Session Ownership Validation

- `uploadMultipartPart`、`completeMultipartUpload`、`abortMultipartUpload` 必须校验分片会话归属
- 归属校验至少固定包含：
  - `tenantNo`
  - `ownerType`
  - `ownerId`
- 仅凭 `uploadId` 不得继续写分片、完成上传或取消上传
- 验收标准：
  - 非归属方请求固定失败
  - 归属校验失败时不得写底层对象、不得更新会话状态

#### P0-3 Multipart Integrity Validation

- `completeMultipartUpload` 前必须校验分片完整性
- 固定校验项：
  - 分片号连续
  - 分片总大小等于 `totalSize`
  - 会话状态允许完成
  - 分片记录数与 `uploadedPartCount` 一致
- 本地文件模式和 `OSS` 模式必须共用同一套完整性规则
- 验收标准：
  - 缺片、乱序、总大小不匹配、重复完成固定失败
  - 失败时不得生成正式 `StoredObject`

#### P0-4 Delete Consistency Safeguard

- 删除流程不得采用“先删底层对象，再更新数据库状态”的不可补偿顺序直接上线
- 固定整改方向二选一：
  - 先把对象状态迁移到删除中间态，再异步删除底层对象
  - 保留同步删除，但必须增加失败补偿和定期对账修复
- 验收标准：
  - 任一阶段失败后，不允许出现“表里 `ACTIVE`、底层对象已丢失”的不可恢复状态

### 5.2 P1 High Priority Items

#### P1-1 External Controller Exposure Cleanup

- `Storage` 对外业务语义接口必须收口
- `/storage/objects/**` 不得作为业务前端长期依赖接口
- 若保留外部 controller，必须明确其仅用于管理面或调试面，并补权限边界
- 验收标准：
  - 对外开放面和文档边界一致

#### P1-2 Multipart Timeout Cleanup

- 必须新增未完成分片上传会话清理任务
- 清理范围固定包括：
  - `ABORTED` 会话临时文件
  - 超时 `INITIATED/UPLOADING` 会话
  - `OSS` provider 侧未完成 multipart upload
- 验收标准：
  - 超时会话可被自动清理
  - 清理动作可观测、可追踪

#### P1-3 Streaming Memory Control

- 远程门面上传链路不得对大文件或大分片无上限 `readAllBytes`
- 普通上传和分片上传都必须明确内存占用策略
- 验收标准：
  - 上传大文件时不会因单次请求把全部内容一次性读入堆内存

#### P1-4 Deleted Object Semantics

- 已删除对象的查询、引用建立和访问端点返回语义必须固定
- 固定约束：
  - 已删除对象不得再建立新引用
  - 查询已删除对象时必须明确是否返回 `404` 或受限 DTO
- 验收标准：
  - 删除后对象状态语义一致，不允许业务继续把已删除对象当成可用资源

### 5.3 P2 Stability And Governance Items

#### P2-1 Audit Reliability Governance

- 审计写入失败虽然不阻断主业务，但必须补充可观测和补偿手段
- 固定补齐项：
  - metric
  - 告警主键
  - 死信或 outbox
  - 回放入口
- 验收标准：
  - 审计失败可统计、可告警、可回补

#### P2-2 Compatibility Field Cleanup

- `accessUrl` 兼容字段必须有明确下线计划
- 固定治理要求：
  - 文档主口径只保留 `accessEndpoint`
  - 新代码不得新增对 `accessUrl` 的直接依赖
- 验收标准：
  - 对外兼容期结束后可移除 `accessUrl`

#### P2-3 Test Coverage Completion

- `Storage` 模块必须补齐应用层和基础设施层测试
- 固定补齐范围：
  - 普通上传
  - 建立引用
  - 清理引用
  - 删除对象
  - 分片初始化
  - 分片上传
  - 分片完成
  - 分片取消
  - remote facade/provider 路径契约
- 验收标准：
  - `storage` 主流程和关键异常路径均有自动化测试

## 6. Non-Functional Requirements

- 所有 `P0/P1` 整改项必须补日志、metric 或测试三者之一，且不能只靠人工验证
- 所有和 `OSS` 相关的整改必须在 `MinIO` 本地环境和抽象接口层双重验证
- 所有异常路径必须明确失败后是否回滚数据库、是否清理底层对象、是否进入补偿队列

## 7. Open Items

无
