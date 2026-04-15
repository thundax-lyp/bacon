## 模块结构与代码风格差异

### 范围

- `inventory`
- `payment`
- `order`
- `storage`
- `upms`

### 总结

五个模块的 Maven 分层骨架已经统一为 `api / interfaces / application / domain / infra`，但层内手法还没有完全统一。当前差异主要集中在：

- `interfaces` 入口形态
- `application` 输入边界
- DTO 装配位置
- 异常风格
- 租户约束显式性
- controller 命名与路径规则

---

### 1. controller 命名与路径规则不一致

#### 现状

- `payment` 更偏用例型命名，如 `PaymentQueryController`
- `inventory`、`storage`、`upms` 更偏聚合型命名，如 `InventoryController`、`StorageController`、`UserController`
- `order` 当前也是聚合型 controller，但根路径仍是 `/order`

路径风格也不一致：

- `/payment`
- `/order`
- `/storage/objects`
- `/inventory/inventories`
- `/upms/users`

#### 影响

- 后续很难统一 controller 命名规范
- 很难补稳定的 ArchUnit / lint 规则

---

### 2. 分页与过滤入口风格未完全统一

#### 现状

- `inventory` 已较稳定使用 `@ModelAttribute *PageRequest`
- `order` 已改到 `OrderPageRequest`
- `storage` 使用 `StoredObjectPageRequest`，但过滤字段仍多是原始字符串
- `upms` 多数分页也走 `*PageRequest`，但 `/page` 子路径使用更重
- `payment` 查询接口较轻，仍以 `@PathVariable + @RequestParam` 为主

#### 影响

- 同类查询接口的入参风格不统一
- OpenAPI 展示与校验位置不统一

---

### 3. interfaces -> application 的输入边界强弱不一

#### 现状

- `inventory`、`order` 更接近 `interfaces` 做 `plain type -> codec / enum / VO`
- `storage`、`payment` 的 application 仍吃较多稳定原始类型
- `upms` 正在收敛，但仍是混合态

#### 影响

- 模块间 application contract 风格不一致
- VO 边界和协议边界容易继续漂移

---

### 4. DTO 装配位置不一致

#### 现状

- `inventory`、`payment`、`storage` 的 assembler 使用更稳定
- `order` 仍有明显的 application service 内部本地映射
- `upms` 已收敛一轮，但历史上 application service 内手写 DTO 较多，仍需持续清理

#### 影响

- application service 容易同时承担“编排 + DTO 拼装 + 协议适配”
- 很难形成稳定的 assembler 约束

---

### 5. application service 厚度差异明显

#### 现状

- `payment`、`storage` query service 较薄
- `inventory` 中等厚度，但结构清楚
- `order` query service 偏厚，尤其是详情与 snapshot 装配
- `upms` command service 最厚，职责跨度最大

#### 影响

- 模块可维护性差异大
- review 和架构规则难用一套标准衡量

---

### 6. 异常风格未统一

#### 现状

- `inventory`、`payment` 更偏域异常或稳定异常
- `storage` 多用 `NotFoundException`
- `order`、`upms` 仍保留较多 `IllegalArgumentException`

#### 影响

- 业务错误语义表达不一致
- 模块间 HTTP 语义和错误码治理基础不同

---

### 7. 租户约束显式性不一致

#### 现状

- `inventory` 在 application/query 层显式 `BaconContextHolder.requireTenantId()`
- `order` 只在部分方法显式体现
- `payment`、`storage` 从表层代码上看租户语义更弱
- `upms` 同时存在平台级和租户级能力，边界天然更复杂

#### 影响

- 多租户约束位置不统一
- 很难抽出统一的租户安全基线

---

### 8. 横切注解密度不一致

#### 现状

- `upms` 大量使用 `@SysLog`
- 其他业务域多以 `@HasPermission + @Operation` 为主

#### 影响

- 模块在审计、日志、后台管理语义上的风格差异明显

---

### 当前判断

如果按“层次清晰、边界稳定、风格自洽”排序：

- 第一梯队：`inventory`、`payment`
- 第二梯队：`storage`
- 第三梯队：`order`
- 最复杂：`upms`

---

### 后续最值得统一的方向

1. 统一 controller 命名和根路径规则
2. 统一 application 输入边界
3. 统一 DTO 装配位置
4. 统一异常风格
5. 统一租户约束显式性
