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

---

## TODO List

### P0 - 跨域契约先收口

- [ ] `upms-api`：把 `UserReadFacade` 中直接暴露的 `UserId`、`UserIdentityType` 改成稳定 facade 入参
  - 输出物：新的 facade method signature、对应 local impl / remote impl 调整、调用方编译通过
  - 验收点：`api` 不再直接依赖 `upms.domain` 类型
  - 重要度：9/10

- [ ] `storage-api`：去掉 `StoredObjectFacade` 里对 `objectId` 的字符串魔法协议依赖
  - 现状问题：local impl 里存在 `"O"` 前缀剥离逻辑
  - 输出物：统一的 objectId 表达方式，以及 facade/local/remote 调整
  - 验收点：facade 不再需要隐式解析 `"O123"` 这种实现细节
  - 重要度：9/10

- [ ] 补一条跨域契约约束文档或检查规则：`api.facade` 不允许直接引用 domain entity / domain enum / domain VO
  - 输出物：文档规则或 ArchUnit 检查
  - 验收点：后续新增 facade 不再继续漂移
  - 重要度：8/10

### P0 - `upms` 先拆大类

- [ ] 拆分 `UserController`：把用户基础信息、密码、角色、头像、导入导出拆成独立 controller
  - 输出物：多个小 controller，原路由语义保持不变或有明确迁移方案
  - 验收点：单个 controller 不再同时承担 8+ 类职责
  - 重要度：9/10

- [ ] 拆分 `UserApplicationService`：先拆出 `UserQueryApplicationService`
  - 范围：`getUserById`、`getUserIdentity`、`getUserLoginCredential`、`pageUsers`、`getRolesByUserId`、`exportUsers`
  - 验收点：查询逻辑不再和写操作混在同一个类里
  - 重要度：9/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserProfileApplicationService`
  - 范围：`createUser`、`updateUser`、`updateUserStatus`、`deleteUser`
  - 验收点：用户基础资料写操作聚合到单独服务
  - 重要度：8/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserPasswordApplicationService`
  - 范围：`initPassword`、`resetPassword`、`changePassword`
  - 验收点：密码流程与资料维护彻底分开
  - 重要度：8/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserAvatarApplicationService`
  - 范围：`getAvatarAccessUrl`、`updateAvatar`
  - 验收点：文件上传与用户资料主流程分离
  - 重要度：7/10

- [ ] 拆分 `UserApplicationService`：再拆出 `UserImportExportApplicationService`
  - 范围：`importUsers`、`exportUsers`
  - 验收点：批量导入导出不再堆在核心用户服务中
  - 重要度：7/10

### P1 - `upms` 仓储职责回收

- [ ] 把 `UserRepositoryImpl` 中的“账号/手机号身份替换流程”从 repository impl 提升到 application 层编排
  - 目标类：`replaceAccountIdentity`、`replacePhoneIdentity` 相关流程
  - 验收点：repository 只保留存取动作，不再主导业务步骤顺序
  - 重要度：8/10

- [ ] 把 `UserRepositoryImpl` 中的密码策略装配从 repository impl 提升到 application 或独立 domain service
  - 目标类：默认密码、密码过期时间、失败上限、needChangePassword 规则
  - 验收点：密码规则不再写死在 infra
  - 重要度：8/10

- [ ] 把 `UserRepositoryImpl` 中角色绑定后的缓存清理从 repository impl 外提
  - 目标：让 repository 返回结果，缓存失效由 application 编排
  - 验收点：infra 不再混入明显横切业务动作
  - 重要度：7/10

### P1 - 统一 application 输入边界

- [ ] `payment`：为创建支付引入 `CreatePaymentCommand`
  - 替换对象：`PaymentCreateApplicationService#createPayment`
  - 验收点：不再使用长参数列表传递 `orderNo/userId/amount/channelCode/subject/expiredAt`
  - 重要度：7/10

- [ ] `payment`：为关单引入 `ClosePaymentCommand`
  - 替换对象：`PaymentCloseApplicationService#closePayment`
  - 验收点：命令应用服务统一使用 command 入参
  - 重要度：6/10

- [ ] `inventory`：为 facade 入站场景补应用层 command，减少 application 直接吃 `api.dto`
  - 替换对象：`InventoryApplicationService`
  - 验收点：application 层不再直接依赖 `InventoryReservationItemDTO`
  - 重要度：7/10

- [ ] 形成统一约束：application 公共入口优先使用 `Command / Query / VO`，禁止新增多 primitive 长参数方法
  - 输出物：规则文档或代码检查项
  - 重要度：6/10

### P1 - `order` 先补接口层一致性

- [ ] 给 `OrderController` 补 `@Validated`
  - 验收点：controller 层参数约束与 `inventory/payment/storage` 对齐
  - 重要度：6/10

- [ ] 给 `OrderController#create` 补 `@Valid`
  - 验收点：创建订单请求的字段校验前置到 interfaces
  - 重要度：6/10

- [ ] 给 `OrderController#cancel` 补 `@Valid`
  - 验收点：取消原因等参数不再完全依赖 application 兜底
  - 重要度：5/10

- [ ] 逐个检查 `OrderController` 的 `@PathVariable` / `@RequestBody` 参数约束是否完整
  - 输出物：缺失的 `@NotBlank`、`@Positive` 等补齐
  - 重要度：5/10

### P2 - DTO 装配位置统一

- [ ] `order`：把 `OrderCreateApplicationService` 中手写 `OrderSummaryDTO` 装配迁移到 assembler
  - 验收点：application service 只保留编排和状态推进
  - 重要度：6/10

- [ ] `order`：继续检查 query application service 是否还存在 response/dto 手写拼装
  - 验收点：详情、分页、snapshot 装配逐步收敛到 assembler
  - 重要度：6/10

- [ ] `upms`：排查 application service 内是否还有历史遗留 DTO 手写映射
  - 验收点：统一回收进 assembler
  - 重要度：5/10

### P2 - 异常风格统一

- [ ] `order`：清理 command/query/application 中直接抛出的 `IllegalArgumentException`
  - 目标：替换成稳定业务异常或参数异常
  - 重要度：6/10

- [ ] `upms`：清理 application / repository 中直接抛出的 `IllegalArgumentException`
  - 目标：区分“参数非法 / 资源不存在 / 业务冲突”
  - 重要度：6/10

- [ ] 补一份异常使用约定
  - 范围：参数错误、领域错误、资源不存在、权限错误
  - 验收点：后续新代码不再随意混用
  - 重要度：5/10

### P2 - controller 与路径规范

- [ ] 定一版 controller 命名规范
  - 需要决策：采用“聚合型 controller”还是“用例型 controller”
  - 验收点：新代码命名不再摇摆
  - 重要度：5/10

- [ ] 定一版根路径规范
  - 需要决策：是否统一复数资源路径、是否统一 `/page`
  - 验收点：后续新增接口按统一规则落地
  - 重要度：5/10

- [ ] 先列出现有不一致路径清单，再决定是否批量重构
  - 范围：`/payment`、`/order`、`/storage/objects`、`/inventory/inventories`、`/upms/users`
  - 重要度：4/10

### P2 - 租户边界统一

- [ ] 梳理五个域 application 层中 `requireTenantId` 的落点
  - 输出物：一张方法级清单
  - 重要度：6/10

- [ ] 明确 `upms` 中平台级能力和租户级能力的边界
  - 输出物：哪些接口必须要求租户，哪些接口允许平台上下文
  - 重要度：6/10

- [ ] 补一条实现规则：跨域写操作默认显式校验租户上下文
  - 重要度：5/10

### P3 - 持续治理

- [ ] 增加 ArchUnit 或同类检查：`api` 不直接依赖 domain
  - 重要度：5/10

- [ ] 增加 ArchUnit 或同类检查：`application` 不直接接收 `interfaces.dto.request/response`
  - 重要度：5/10

- [ ] 增加 ArchUnit 或同类检查：`infra.repository.impl` 不承载明显业务编排
  - 重要度：5/10

- [ ] 评估 `@SysLog` 的统一策略
  - 需要决策：仅后台管理域保留，还是其他核心域也补齐
  - 重要度：4/10

### 建议执行顺序

1. 先改 `upms-api` 和 `storage-api` 的 facade 契约
2. 再拆 `upms` 的 `UserController` 和 `UserApplicationService`
3. 然后把 `upms` 的 repository 业务逻辑上移
4. 接着统一 `payment / inventory` 的 application 入参风格
5. 然后补 `order` 的 interfaces 校验和 assembler 收敛
6. 最后统一异常、租户边界、路径规范和架构检查
