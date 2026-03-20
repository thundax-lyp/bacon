# AUTH REQUIREMENTS

## 1. Purpose

Auth 是 Bacon 的统一认证与会话业务域。  
本文档定义 Auth 模块的需求边界、实现约束和稳定契约。  
本文档是后续设计、任务拆解、实现和测试的唯一基线。  
当前范围内全部功能属于同一交付范围，不做分期交付。

## 2. Scope

### 2.1 In Scope

- 账号密码登录
- 手机短信登录
- 企业微信扫码登录
- `GitHub OAuth2` 登录
- 访问令牌签发
- 刷新令牌签发
- 刷新令牌换发
- 登出
- 当前会话查询
- 当前用户本人修改密码
- 登录失败处理
- 会话校验
- 会话失效
- 认证审计日志

### 2.2 Out Of Scope

- 用户主数据维护
- `UserIdentity` 主数据维护
- 管理员初始化密码
- 管理员重置密码
- 管理员修改他人密码
- 角色、菜单、资源、数据权限维护
- SSO
- 除 `GitHub OAuth2` 外的其他社交登录
- C 端账号体系

## 3. Bounded Context

### 3.1 Auth

- `Auth` 负责登录、令牌签发、令牌刷新、登出、会话校验和认证流程
- `Auth` 负责用户本人修改密码
- `Auth` 负责认证审计日志
- `Auth` 不拥有 `User`、`UserIdentity`、`Tenant`、`Role`、`Menu`、`Resource` 主数据
- `Auth` 不拥有权限主数据
- `Auth` 不直接维护管理员密码管理能力

### 3.2 UPMS

- `UPMS` 负责 `User`、`UserIdentity`、`Tenant`、`Department`、`Post`、`Role`、`Menu`、`Resource`
- `UPMS` 负责密码数据存储
- `UPMS` 负责管理员初始化密码、重置密码、修改密码
- `UPMS` 负责用户状态、租户状态、授权结果和数据权限结果

### 3.3 Gateway

- `Gateway` 负责接收客户端请求并执行统一认证拦截
- `Gateway` 不拥有认证主数据
- `Gateway` 不持久化会话
- `Gateway` 只消费 `Auth` 提供的认证结果或令牌校验结果

### 3.4 Cross-Domain Rule

- `Auth` 只能依赖 `bacon-upms-api`
- `Auth` 不得依赖 `UPMS` 内部实现
- `Auth` 不得在 `token payload` 中保存权限数据
- `Auth` 认证成功后必须通过 `UserIdentity` 定位唯一 `User`
- `Auth` 认证成功后如需权限数据，只能通过 `PermissionReadFacade` 读取
- 单体模式使用本地 `Facade` 实现
- 微服务模式使用远程 `Facade` 实现，并保持同一契约

## 4. Module Mapping

### 4.1 `bacon-auth-api`

- 跨域 `Facade`
- `DTO`
- 对外共享枚举

固定接口：

- `TokenVerifyFacade`
- `SessionCommandFacade`

`TokenVerifyFacade` 固定方法：

- `verifyAccessToken(accessToken)`，返回固定 `DTO`
- `getSessionContext(sessionId)`，返回固定 `DTO`

`verifyAccessToken` 返回值至少包含：

- `valid`
- `userId`
- `tenantId`
- `sessionId`
- `identityId`
- `identityType`

`getSessionContext` 返回值至少包含：

- `sessionId`
- `userId`
- `tenantId`
- `sessionStatus`
- `lastAccessTime`

`SessionCommandFacade` 固定方法：

- `invalidateUserSessions(tenantId, userId, reason)`
- `invalidateTenantSessions(tenantId, reason)`

### 4.2 `bacon-auth-interfaces`

- `Controller`
- 请求 `DTO`
- 响应 `VO`
- `Assembler`
- 对外适配端点

固定端点：

- `POST /auth/login/password`
- `POST /auth/login/sms`
- `POST /auth/login/wecom`
- `GET /auth/login/github/callback`
- `POST /auth/token/refresh`
- `POST /auth/logout`
- `POST /auth/password/change`
- `GET /auth/session/current`

### 4.3 `bacon-auth-application`

固定服务：

- `LoginApplicationService`
- `TokenApplicationService`
- `PasswordApplicationService`
- `SessionApplicationService`
- `AuthAuditApplicationService`

### 4.4 `bacon-auth-domain`

- 聚合、实体、值对象
- 领域服务
- `Repository` 接口
- 领域规则和不变量

### 4.5 `bacon-auth-infra`

- 令牌签发实现
- 会话存储实现
- 缓存实现
- 第三方登录适配器
- 短信验证码发送与校验适配器
- 审计日志持久化

### 4.6 `bacon-auth-starter`

- `Auth` 独立启动模块
- 微服务模式运行入口
- 装配 `bacon-auth-*` 与公共基础模块

## 5. Core Domain Objects

- `AuthSession`
- `AccessTokenClaims`
- `RefreshTokenSession`
- `LoginRequest`
- `LoginResult`
- `AuthPrincipal`
- `SmsCaptcha`
- `ThirdPartyIdentityBinding`
- `AuthAuditLog`

## 6. Global Constraints

### 6.1 Tenant And Identity

- `Tenant` 是一级隔离边界
- `AuthSession` 必须带 `tenantId`
- `AuthSession` 必须带 `userId`
- `AuthSession` 必须带 `identityId`
- `AuthSession` 必须带 `identityType`
- `Auth` 只接受来自已启用 `Tenant` 的登录
- `Auth` 只接受来自已启用 `User` 的登录
- `Auth` 只接受来自已启用 `UserIdentity` 的登录
- 同一认证请求必须定位唯一 `User`

### 6.2 Login Type

- 登录方式固定为：账号密码、手机短信、企业微信扫码、`GitHub OAuth2`
- `identityType` 固定为 `ACCOUNT`、`PHONE`、`WECOM`、`GITHUB`
- 不同登录方式最终必须映射到同一 `User`
- 不同登录方式共享同一会话模型
- 禁用某个 `UserIdentity` 只影响该登录方式，不等于禁用 `User`

### 6.3 Token Strategy

- 登录成功后必须同时签发 `access token` 和 `refresh token`
- `access token` 固定为签名 `JWT`
- `refresh token` 固定为高强度随机字符串
- `access token` 与 `refresh token` 必须绑定同一 `sessionId`
- `token payload` 只保存身份信息和会话定位信息，不保存权限数据
- `token payload` 至少包含 `userId`、`tenantId`、`sessionId`、`identityId`、`identityType`、`issuedAt`、`expireAt`
- `refresh token` 不得明文落日志
- 同一 `refresh token` 只能成功使用一次
- 刷新令牌换发后，旧 `refresh token` 立即失效

### 6.4 Session Strategy

- 每次成功登录必须创建新的 `AuthSession`
- `AuthSession` 状态固定为 `ACTIVE`、`LOGGED_OUT`、`INVALIDATED`、`EXPIRED`
- `AuthSession` 必须记录创建时间、最后访问时间、失效原因
- `AuthSession` 必须支持按 `tenantId`、`userId`、`sessionId` 查询
- 会话校验必须同时校验令牌签名、过期时间和服务端会话状态
- 用户显式登出后，当前会话立即失效
- 用户停用后，全部活动会话立即失效
- 用户删除后，全部活动会话立即失效
- 管理员初始化密码、重置密码、修改密码后，全部活动会话立即失效
- `Tenant` 停用后，该租户下全部活动会话立即失效
- 会话失效后，用户必须重新登录

### 6.5 Password Rule

- 用户本人修改密码归 `Auth`
- `Auth` 不保存密码主数据
- 用户本人修改密码时必须先校验旧密码
- 新密码规则必须与 `UPMS` 保持一致
- 密码长度至少 `8` 位
- 密码必须同时包含大写字母、小写字母、数字
- 密码可以包含特殊字符
- 新密码不得与旧密码相同
- `Auth` 不得记录明文密码、哈希值、临时密码到日志、审计日志、消息体、缓存
- 用户本人修改密码成功后，当前和其他活动会话立即失效
- 用户本人修改密码后，用户必须重新登录

### 6.6 Authorization Assembly Rule

- `Auth` 登录成功后必须从 `UPMS` 查询用户状态、菜单树、权限码和数据权限上下文
- `Auth` 不拥有权限计算规则
- `Auth` 不缓存权限主数据
- `Auth` 如缓存登录态附属信息，必须与会话生命周期保持一致
- `Auth` 返回给客户端的登录结果必须包含会话建立结果

### 6.7 Failure Handling

- 认证失败必须返回统一失败结果
- 不存在的账号、错误密码、无效短信验证码、无效第三方回调码、用户停用、租户停用必须视为认证失败
- 认证失败不得泄露用户是否存在、密码是否正确、手机号是否已注册等敏感判断细节
- 认证失败必须记录审计日志
- 刷新令牌无效、已使用、已过期、会话已失效时必须拒绝换发

## 7. Functional Requirements

### 7.1 Password Login

- 使用 `account + password` 发起登录
- 通过 `UserIdentity(ACCOUNT)` 定位唯一 `User`
- 校验密码
- 校验 `Tenant`、`User`、`UserIdentity` 状态
- 创建 `AuthSession`
- 返回令牌和当前用户基础认证信息

补充约束：

- `account` 不区分登录入口来源
- 返回结果不得包含密码相关数据

### 7.2 SMS Login

- 使用 `phone + smsCaptcha` 发起登录
- 通过 `UserIdentity(PHONE)` 定位唯一 `User`
- 校验短信验证码
- 校验 `Tenant`、`User`、`UserIdentity` 状态
- 创建 `AuthSession`
- 返回令牌和当前用户基础认证信息

补充约束：

- 短信验证码必须一次性使用
- 短信验证码校验成功后立即失效

### 7.3 WECOM Login

- 通过企业微信扫码回调完成登录
- 通过企业微信身份标识定位 `UserIdentity(WECOM)`
- 校验 `Tenant`、`User`、`UserIdentity` 状态
- 创建 `AuthSession`
- 返回令牌和当前用户基础认证信息

补充约束：

- 无有效绑定关系时登录必须失败
- 第三方临时授权码只能使用一次

### 7.4 `GitHub OAuth2` Login

- 通过 `GitHub OAuth2` 授权码回调完成登录
- 通过 `GitHub` 身份标识定位 `UserIdentity(GITHUB)`
- 校验 `Tenant`、`User`、`UserIdentity` 状态
- 创建 `AuthSession`
- 返回令牌和当前用户基础认证信息

补充约束：

- 无有效绑定关系时登录必须失败
- 不支持首次登录自动注册

### 7.5 Token Refresh

- 使用 `refresh token` 换发新的 `access token` 和 `refresh token`
- 校验当前会话状态
- 校验 `refresh token` 是否属于当前会话
- 换发成功后返回新的令牌对

补充约束：

- 刷新操作必须幂等失败，不得重复成功
- 刷新成功后旧 `refresh token` 立即失效

### 7.6 Logout

- 用户可主动登出当前会话
- 登出后当前 `AuthSession` 状态改为 `LOGGED_OUT`
- 登出后当前 `access token` 和 `refresh token` 均不得再使用

补充约束：

- 重复登出不得产生脏数据
- 登出操作必须记录审计日志

### 7.7 Self Password Change

- 当前登录用户可修改本人密码
- 必须校验旧密码
- 必须校验新密码复杂度
- 修改成功后失效全部活动会话

补充约束：

- 不允许绕过旧密码校验
- 修改密码成功必须记录审计日志

### 7.8 Session Validation

- `Auth` 必须提供令牌校验能力
- 校验成功后返回稳定的会话上下文
- 校验失败时返回明确的失败状态

补充约束：

- 校验结果必须可被 `Gateway` 和其他服务复用
- 校验逻辑不得依赖权限数据

### 7.9 Current Session Query

- 返回当前登录用户的基础会话信息
- 返回当前会话状态、登录方式、登录时间、最近访问时间

补充约束：

- 不返回密码、刷新令牌、第三方敏感凭据

### 7.10 Auth Audit Log

- 记录登录成功
- 记录登录失败
- 记录刷新令牌成功和失败
- 记录登出
- 记录本人修改密码
- 记录会话批量失效

必须记录的字段：

- `tenantId`
- `userId`
- `identityId`
- `identityType`
- `sessionId`
- 操作时间
- 操作类型
- 请求来源
- 客户端 `ip`
- `userAgent`
- 结果状态
- 失败原因摘要

补充约束：

- 审计日志必须持久化存储
- 审计日志必须可按 `tenantId`、`userId`、`identityType`、操作类型、结果状态、时间范围查询
- 审计日志不得记录明文密码、哈希值、短信验证码、刷新令牌、第三方访问令牌
- 审计日志写入失败不得影响主业务提交结果

### 7.11 Cache

缓存键：

- `auth:session:{sessionId}`
- `auth:refresh-token:{refreshTokenHash}`
- `auth:user-sessions:{tenantId}:{userId}`
- `auth:tenant-sessions:{tenantId}`

缓存内容：

- 会话缓存保存 `AuthSession` 核心状态
- 刷新令牌缓存保存 `refresh token` 哈希、会话绑定关系、过期时间和使用状态
- 用户会话索引缓存保存用户活动 `sessionId` 集合
- 租户会话索引缓存保存租户活动 `sessionId` 集合

失效触发：

- 登录成功且旧同端策略要求替换会话时
- 用户主动登出
- 刷新令牌换发
- 用户本人修改密码
- 管理员初始化密码
- 管理员重置密码
- 管理员修改密码
- 用户停用
- 用户删除
- 租户停用
- 会话自然过期

补充约束：

- 缓存以主动失效为主，`TTL` 为兜底
- `refresh token` 只保存哈希值，不保存明文
- 会话缓存和刷新令牌缓存不得混合存储
- 影响会话有效性的变更必须主动失效相关缓存

## 8. Key Flows

### 8.1 Password Login

1. 客户端提交 `account + password`
2. `Auth` 通过 `UserIdentity(ACCOUNT)` 定位唯一 `User`
3. `Auth` 校验密码和状态
4. `Auth` 创建 `AuthSession`
5. `Auth` 从 `UPMS` 读取菜单树、权限码和数据权限上下文
6. `Auth` 签发 `access token` 和 `refresh token`
7. 客户端携带 `access token` 访问受保护资源

### 8.2 Token Refresh

1. 客户端提交 `refresh token`
2. `Auth` 校验 `refresh token`、会话状态和过期时间
3. `Auth` 使旧 `refresh token` 失效
4. `Auth` 换发新的令牌对
5. 客户端使用新的令牌继续访问

### 8.3 Logout

1. 客户端发起登出
2. `Auth` 定位当前 `sessionId`
3. `Auth` 将当前会话置为 `LOGGED_OUT`
4. `Auth` 失效会话和刷新令牌缓存
5. 用户后续访问受保护资源时认证失败

### 8.4 Self Password Change

1. 当前登录用户提交旧密码和新密码
2. `Auth` 校验旧密码和新密码规则
3. `Auth` 调用 `UPMS` 完成密码持久化更新
4. `Auth` 失效该用户全部活动会话
5. 用户后续访问受保护资源时认证失败
6. 用户必须重新登录

### 8.5 User Or Tenant Invalidation

1. `UPMS` 或上游管理动作触发用户停用、用户删除、管理员改密或租户停用
2. `Auth` 接收失效指令
3. `Auth` 批量失效相关活动会话和刷新令牌
4. 受影响用户后续访问受保护资源时认证失败
5. 未删除用户如状态恢复，必须重新登录

## 9. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-001 | Security | 所有认证接口必须使用统一安全基线并防止敏感信息泄露 |
| NFR-002 | Security | `token payload` 不得包含权限数据和密码相关数据 |
| NFR-003 | Consistency | 会话失效后，后续认证请求必须立即感知失效结果 |
| NFR-004 | Performance | 高频会话校验必须优先走缓存 |
| NFR-005 | Availability | 认证流程不能成为登录瓶颈 |
| NFR-006 | Architecture | 必须同时支持单体和微服务装配 |
| NFR-007 | Maintainability | 实现必须严格遵守 `interfaces -> application -> domain -> infra` 分层 |
| NFR-008 | Compatibility | `Facade + DTO` 契约必须同时支持本地和远程实现 |
| NFR-009 | Auditability | 认证审计日志必须持久化、可检索、可追溯 |
| NFR-010 | Cache Consistency | 影响会话有效性的变更必须主动失效相关缓存 |
| NFR-011 | Password Security | 明文密码、短信验证码、刷新令牌不得进入日志、审计日志、消息体和缓存明文 |

## 10. Open Items

- `access token` 过期时长未确认
- `refresh token` 过期时长未确认
- 手机短信验证码的发送渠道、签名和模板未确认
- 企业微信扫码登录的回调地址与企业配置未确认
- `GitHub OAuth2` 的回调地址、`clientId`、授权范围未确认
- 是否需要限制单用户并发会话数未确认
