# AUTH REQUIREMENTS

## 1. Purpose

Auth 是 Bacon 的统一认证与会话业务域。  
本文档定义 Auth 模块的需求边界、实现约束和稳定契约。  
本文档用于指导设计、实现和测试。

## 2. Scope

### 2.1 In Scope

- 账号密码登录
- 手机短信登录
- 企业微信扫码登录
- 微信登录
- `GitHub OAuth2` 登录
- 向第三方应用提供 `OAuth2` 授权服务
- 访问令牌签发
- 刷新令牌签发
- 刷新令牌换发
- 登出
- 当前会话查询
- 当前用户本人修改密码
- 首次登录强制改密校验
- 密码过期校验
- 多因子认证登录编排
- 凭据锁定校验
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
- `OpenID Connect Discovery`
- 动态 `OAuthClient` 注册
- 除 `GitHub OAuth2`、微信登录、企业微信扫码外的其他社交登录
- C 端账号体系

## 3. Bounded Context

### 3.1 Auth

- `Auth` 负责登录、令牌签发、令牌刷新、登出、会话校验和认证流程
- `Auth` 负责用户本人修改密码
- `Auth` 负责多因子认证流程编排
- `Auth` 负责凭据失败后的锁定写回编排
- `Auth` 负责向第三方应用提供标准 `OAuth2` 授权能力
- `Auth` 负责认证审计日志
- `Auth` 不拥有 `User`、`UserIdentity`、`Tenant`、`Role`、`Menu`、`Resource` 主数据
- `Auth` 不拥有权限主数据
- `Auth` 不直接维护管理员密码管理能力

### 3.2 UPMS

- `UPMS` 负责 `User`、`UserIdentity`、`UserCredential`、`Tenant`、`Department`、`Post`、`Role`、`Menu`、`Resource`
- `UPMS` 负责授权关系、数据权限、只读查询能力
- `UPMS` 负责密码数据、凭据状态、多因子因子配置和凭据锁定主数据
- `UPMS` 负责管理员初始化密码、重置密码、修改密码
- `UPMS` 负责用户状态、租户状态、授权结果和数据权限结果

### 3.3 Gateway

- `Gateway` 负责接收客户端请求并执行统一认证拦截
- `Gateway` 不拥有认证主数据
- `Gateway` 不持久化会话
- `Gateway` 只消费 `Auth` 提供的认证结果或令牌校验结果

### 3.4 Third-Party Application

- 第三方应用是 `Auth` 的授权客户端
- 第三方应用通过 `OAuth2 client` 身份接入 `Auth`
- 第三方应用固定为服务端应用
- 第三方应用不直接访问 `UPMS` 主数据
- 第三方应用只能消费 `Auth` 签发的授权码、访问令牌和用户授权结果

### 3.5 Cross-Domain Rule

- `Auth` 只能依赖 `bacon-upms-api`
- `Auth` 不得依赖 `UPMS` 内部实现
- `Auth` 不得在 `token payload` 中保存权限数据
- `Auth` 认证成功后必须通过 `UserIdentity` 定位唯一 `User`
- `Auth` 读取 `User`、`UserIdentity`、`UserCredential`、`Tenant` 状态时，只能通过 `UserReadFacade`
- `Auth` 写回凭据失败次数和锁定清零时，只能通过 `UserCredentialCommandFacade`
- `Auth` 认证成功后如需权限数据，只能通过 `PermissionReadFacade` 读取
- `UPMS` 触发的用户停用、用户删除、管理员初始化密码、管理员重置密码、管理员修改密码、租户停用，必须通过 `Auth` 暴露的会话失效契约触发即时失效
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
- `OAuthClientReadFacade`

`TokenVerifyFacade` 固定方法：

- `verifyAccessToken(accessToken)`，返回固定 `SessionValidationResponse`
- `getSessionContext(sessionId)`，返回固定 `CurrentSessionResponse`

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
- `invalidateSession(sessionId, reason)`

`OAuthClientReadFacade` 固定方法：

- `getClientByClientId(clientId)`，返回固定 `OAuthClientDTO`

`getClientByClientId` 返回值至少包含：

- `clientId`
- `clientName`
- `grantTypes`
- `scopes`
- `redirectUris`
- `enabled`

`SessionCommandFacade` 使用方固定为：

- `UPMS`
- `Gateway`
- 其他经架构评审确认需要执行认证态即时失效的业务域

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
- `GET /oauth2/authorize`
- `POST /oauth2/authorize/decision`
- `POST /oauth2/token`
- `POST /oauth2/introspect`
- `POST /oauth2/revoke`
- `GET /oauth2/userinfo`
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
- `OAuth2AuthorizationApplicationService`
- `OAuth2ClientApplicationService`
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
- `OAuthClient`
- `OAuthAuthorizationCode`
- `OAuthAccessToken`
- `OAuthRefreshToken`
- `OAuthConsent`
- `LoginRequest`
- `LoginResult`
- `AuthPrincipal`
- `SmsCaptcha`
- `ThirdPartyIdentityBinding`
- `AuthAuditLog`

## 5.1 Fixed Enums

- `identityType` 固定为 `ACCOUNT`、`EMAIL`、`PHONE`、`GITHUB`、`WECHAT`、`WECOM`
- `loginType` 固定为 `PASSWORD`、`SMS`、`WECHAT`、`WECOM`、`GITHUB`
- `secondFactorType` 固定为 `TOTP`
- `sessionStatus` 固定为 `ACTIVE`、`LOGGED_OUT`、`INVALIDATED`、`EXPIRED`
- 用户登录态 `tokenStatus` 固定为 `ACTIVE`、`USED`、`INVALIDATED`、`EXPIRED`
- 第三方 `OAuth2 tokenStatus` 固定为 `ACTIVE`、`USED`、`REVOKED`、`EXPIRED`
- `decision` 固定为 `APPROVE`、`REJECT`
- `OAuthClient.clientType` 固定为 `CONFIDENTIAL`

## 5.2 Terminology

- 未特别说明时，`access token` 和 `refresh token` 指用户登录态令牌
- 面向第三方应用的令牌统一写作 `OAuth2 access token` 和 `OAuth2 refresh token`
- `token payload` 仅指用户登录态 `JWT claims`
- `authorization code` 仅指第三方应用 `OAuth2` 授权码
- `session` 仅指用户登录会话，不指第三方应用授权码或第三方令牌
- `tenantId` 固定承载 `TenantId` 文本值
- `userId` 在 `Auth` 中固定承载 `UserId` 文本值

## 5.3 Fixed Response Contracts

- `UserLoginResponse` 至少包含 `accessToken`、`refreshToken`、`tokenType`、`expiresIn`、`sessionId`、`userId`、`tenantId`
- `UserLoginResponse.needChangePassword` 只在账号密码登录场景返回
- `UserTokenRefreshResponse` 至少包含 `accessToken`、`refreshToken`、`tokenType`、`expiresIn`、`sessionId`
- `OAuthClientDTO` 至少包含 `clientId`、`clientName`、`grantTypes`、`scopes`、`redirectUris`、`enabled`
- `OAuth2TokenResponse` 至少包含 `access_token`、`token_type`、`expires_in`、`refresh_token`、`scope`
- `OAuth2IntrospectionResponse` 至少包含 `active`、`client_id`、`scope`、`sub`、`tenant_id`、`exp`
- `OAuth2UserinfoResponse` 至少包含 `sub`、`tenant_id`
- `OAuth2UserinfoResponse.name` 只在授予 `profile` 时返回
- `SessionValidationResponse` 至少包含 `valid`、`tenantId`、`userId`、`sessionId`、`identityType`、`expireAt`
- `CurrentSessionResponse` 至少包含 `sessionId`、`tenantId`、`userId`、`identityType`、`loginType`、`sessionStatus`、`issuedAt`、`lastAccessTime`、`expireAt`

## 5.4 Fixed Fields

- `AuthSession` 至少包含 `id`、`sessionId`、`tenantId`、`userId`、`identityId`、`identityType`、`sessionStatus`、`loginType`、`issuedAt`、`lastAccessTime`、`expireAt`、`logoutAt`、`invalidateReason`
- `AccessTokenClaims` 至少包含 `sub`、`sessionId`、`tenantId`、`userId`、`identityId`、`identityType`、`iat`、`exp`、`iss`
- `RefreshTokenSession` 至少包含 `sessionId`、`refreshTokenHash`、`tokenStatus`、`issuedAt`、`expireAt`、`usedAt`
- `OAuthClient` 至少包含 `id`、`clientId`、`clientSecret`、`clientName`、`clientType`、`grantTypes`、`scopes`、`redirectUris`、`accessTokenTtlSeconds`、`refreshTokenTtlSeconds`、`enabled`、`contact`、`remark`、`createdAt`、`updatedAt`
- `OAuthAuthorizationCode` 至少包含 `authorizationCode`、`clientId`、`tenantId`、`userId`、`redirectUri`、`scopes`、`codeChallenge`、`codeChallengeMethod`、`issuedAt`、`expireAt`、`used`
- `OAuthAccessToken` 至少包含 `tokenId`、`tokenHash`、`clientId`、`tenantId`、`userId`、`scopes`、`issuedAt`、`expireAt`、`tokenStatus`
- `OAuthRefreshToken` 至少包含 `tokenId`、`tokenHash`、`accessTokenId`、`clientId`、`tenantId`、`userId`、`issuedAt`、`expireAt`、`tokenStatus`
- `OAuthConsent` 至少包含 `clientId`、`tenantId`、`userId`、`grantedScopes`、`grantedAt`
- `AuthAuditLog` 至少包含 `id`、`tenantId`、`userId`、`identityId`、`identityType`、`sessionId`、`clientId`、`actionType`、`resultStatus`、`failureReason`、`requestIp`、`userAgent`、`occurredAt`

## 5.5 Uniqueness And Index Rules

- `AuthSession.sessionId` 全局唯一
- `RefreshTokenSession.refreshTokenHash` 全局唯一
- `OAuthClient.clientId` 全局唯一
- `OAuthAuthorizationCode.authorizationCode` 全局唯一
- `OAuthAccessToken.tokenId` 全局唯一
- `OAuthAccessToken.tokenHash` 全局唯一
- `OAuthRefreshToken.tokenId` 全局唯一
- `OAuthRefreshToken.tokenHash` 全局唯一

- `AuthSession` 必须建立 `(tenantId, userId, sessionStatus)` 索引
- `RefreshTokenSession` 必须建立 `(sessionId, tokenStatus)` 索引
- `OAuthAuthorizationCode` 必须建立 `(clientId, userId, expireAt)` 索引
- `OAuthAccessToken` 必须建立 `(clientId, userId, tokenStatus)` 索引
- `AuthAuditLog` 必须建立 `(tenantId, occurredAt)`、`(userId, occurredAt)`、`(clientId, occurredAt)` 索引

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
- 不同登录方式最终必须映射到同一 `User`
- 不同登录方式共享同一会话模型
- 禁用某个 `UserIdentity` 只影响该登录方式，不等于禁用 `User`

### 6.3 Token Strategy

- 用户登录成功后必须同时签发用户登录态 `access token` 和用户登录态 `refresh token`
- `access token` 固定为签名 `JWT`
- `refresh token` 固定为高强度随机字符串
- 用户登录态 `access token TTL` 固定为 `1800` 秒
- 用户登录态 `refresh token TTL` 固定为 `604800` 秒
- `access token` 与 `refresh token` 必须绑定同一 `sessionId`
- `token payload` 只保存身份信息和会话定位信息，不保存权限数据
- `token payload` 至少包含 `userId`、`tenantId`、`sessionId`、`identityId`、`identityType`、`issuedAt`、`expireAt`
- `JWT iss` 固定为 `bacon-auth`
- `refresh token` 不得明文落日志
- 同一 `refresh token` 只能成功使用一次
- 刷新令牌换发后，旧 `refresh token` 立即失效

### 6.4 OAuth2 Provider Rule

- `Auth` 必须作为标准 `OAuth2 Authorization Server` 向第三方应用提供授权服务
- 第三方应用接入主体固定为 `OAuthClient`
- 第三方应用固定为服务端 `CONFIDENTIAL` client，不支持浏览器纯前端 `PUBLIC` client
- `OAuthClient.clientId` 全局唯一
- `OAuthClient.clientType` 固定为 `CONFIDENTIAL`
- `OAuthClient` 状态字段统一使用 `enabled`
- `clientSecret` 不得明文落日志
- `clientSecret` 固定使用强哈希存储，不明文存储
- `OAuth2` 授权范围固定由 `scope` 表达
- `scope` 必须使用稳定英文标识
- 固定支持 `authorization_code`、`refresh_token`
- 不支持 `implicit`
- 不支持动态 `OAuthClient` 注册
- 不支持 `client_credentials`
- `authorization code TTL` 固定为 `300` 秒
- `OAuth2 access token TTL` 默认固定为 `1800` 秒
- `OAuth2 refresh token TTL` 默认固定为 `2592000` 秒
- 第三方应用授权流程固定要求 `PKCE`
- `code_challenge_method` 固定支持 `S256`
- `OAuth2 refresh token` 固定为一次性使用
- `OAuth2 refresh token` 成功换发后，旧 `OAuth2 refresh token` 立即失效
- 授权码必须一次性使用
- 授权码必须绑定 `clientId`、`userId`、`tenantId`、`redirectUri`、`scopes`
- 未注册的 `redirectUri` 必须拒绝授权
- 已停用 `OAuthClient` 不得发起授权和换发令牌
- 第三方应用访问令牌校验必须支持令牌有效性查询
- 第三方应用令牌撤销后必须立即失效
- 提供 `userinfo` 能力，但不提供完整 `OpenID Connect Discovery`
- 不签发 `id_token`

### 6.5 OAuth2 Scope Mapping

- `scope` 固定支持 `openid`、`profile`
- `openid` 只表示第三方应用可读取稳定用户主体标识
- `profile` 表示第三方应用可读取允许暴露的基础身份信息
- 未授予 `openid` 时，不得访问 `GET /oauth2/userinfo`
- 未授予 `profile` 时，不得返回 `name`
- 当前 `userinfo` 最多返回 `sub`、`tenant_id`、`name`
- `userinfo.sub` 固定映射为 `User.id`
- `userinfo.tenant_id` 固定映射为当前用户所属 `tenantId`

### 6.6 OAuth2 Authorization Request Context

- `GET /oauth2/authorize` 成功校验后，必须生成服务端授权请求上下文
- 授权请求上下文固定使用 `authorizationRequestId` 标识
- `authorizationRequestId` 全局唯一
- 授权请求上下文必须绑定 `clientId`、`redirectUri`、`scope`、`state`、`codeChallenge`、`codeChallengeMethod`、`tenantId`、`userId`
- 授权请求上下文 `TTL` 固定为 `300` 秒
- `POST /oauth2/authorize/decision` 必须基于 `authorizationRequestId` 提交授权决定
- `POST /oauth2/authorize/decision` 不得直接信任前端重复提交的 `redirectUri`、`codeChallenge`、`codeChallengeMethod`
- 授权请求上下文只可使用一次
- 授权请求上下文过期或已使用时，授权确认必须失败

### 6.7 Session Strategy

- 每次成功登录必须创建新的 `AuthSession`
- 单用户并发会话数不设固定上限
- `AuthSession` 必须记录创建时间、最后访问时间、失效原因
- `AuthSession` 必须支持按 `tenantId`、`userId`、`sessionId` 查询
- 会话校验必须同时校验令牌签名、过期时间和服务端会话状态
- 用户显式登出后，当前会话立即失效
- 用户停用后，全部活动会话立即失效
- 用户删除后，全部活动会话立即失效
- 管理员初始化密码、重置密码、修改密码后，全部活动会话立即失效
- `Tenant` 停用后，该租户下全部活动会话立即失效
- 会话失效后，用户必须重新登录

### 6.8 Password Rule

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

### 6.9 Authorization Assembly Rule

- `Auth` 登录成功后必须从 `UPMS` 查询用户状态、菜单树、权限码和数据权限上下文
- `Auth` 不拥有权限计算规则
- `Auth` 不缓存权限主数据
- `Auth` 如缓存登录态附属信息，必须与会话生命周期保持一致
- 登录成功响应必须包含会话建立结果
- 登录成功响应不得直接返回菜单树、权限码、数据权限上下文
- 前端登录成功后，如需菜单树、权限码、数据权限上下文，必须调用 `UPMS` 的只读查询接口获取

### 6.10 Failure Handling

- 认证失败必须返回统一失败结果
- 不存在的账号、错误密码、无效短信验证码、无效第三方回调码、用户停用、租户停用必须视为认证失败
- 认证失败不得泄露用户是否存在、密码是否正确、手机号是否已注册等敏感判断细节
- 认证失败必须记录审计日志
- 刷新令牌无效、已使用、已过期、会话已失效时必须拒绝换发
- `OAuth2` 错误返回必须使用标准错误码语义
- 固定错误码至少包括 `invalid_request`、`invalid_client`、`invalid_grant`、`unauthorized_client`、`invalid_scope`、`access_denied`

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
- 请求体固定包含 `account`、`password`
- 返回结果不得包含密码相关数据
- 成功响应固定使用 `UserLoginResponse`

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
- 请求体固定包含 `phone`、`smsCaptcha`
- 成功响应固定使用 `UserLoginResponse`

### 7.3 WECOM Login

- 通过企业微信扫码回调完成登录
- 通过企业微信身份标识定位 `UserIdentity(WECOM)`
- 校验 `Tenant`、`User`、`UserIdentity` 状态
- 创建 `AuthSession`
- 返回令牌和当前用户基础认证信息

补充约束：

- 无有效绑定关系时登录必须失败
- 第三方临时授权码只能使用一次
- 成功响应固定使用 `UserLoginResponse`

### 7.4 `GitHub OAuth2` Login

- 通过 `GitHub OAuth2` 授权码回调完成登录
- 通过 `GitHub` 身份标识定位 `UserIdentity(GITHUB)`
- 校验 `Tenant`、`User`、`UserIdentity` 状态
- 创建 `AuthSession`
- 返回令牌和当前用户基础认证信息

补充约束：

- 无有效绑定关系时登录必须失败
- 不支持首次登录自动注册
- 成功响应固定使用 `UserLoginResponse`

### 7.5 Token Refresh

- 使用用户登录态 `refresh token` 换发新的用户登录态 `access token` 和用户登录态 `refresh token`
- 校验当前会话状态
- 校验 `refresh token` 是否属于当前会话
- 换发成功后返回新的令牌对

补充约束：

- 刷新操作必须幂等失败，不得重复成功
- 刷新成功后旧 `refresh token` 立即失效
- 请求体固定包含 `refreshToken`
- 成功响应固定使用 `UserTokenRefreshResponse`

### 7.6 OAuth2 Authorization Service

- 向第三方应用提供 `OAuth2` 授权服务
- 支持第三方应用发起授权请求
- 支持用户完成授权确认
- 支持使用授权码换取访问令牌
- 支持访问令牌校验
- 支持访问令牌撤销

补充约束：

- 固定支持 `authorization_code` 和 `refresh_token`
- 授权确认结果必须绑定当前登录用户
- 授权码、访问令牌、刷新令牌必须绑定 `clientId`
- 授权范围不得超出 `OAuthClient` 已注册范围
- `GET /oauth2/authorize` 固定请求参数包含 `response_type=code`、`client_id`、`redirect_uri`、`scope`、`state`、`code_challenge`、`code_challenge_method`
- `GET /oauth2/authorize` 负责登录态检查、客户端校验、授权页展示上下文生成
- `GET /oauth2/authorize` 成功响应至少包含 `authorizationRequestId`、`clientId`、`clientName`、`scope`、`state`
- `POST /oauth2/authorize/decision` 固定请求参数包含 `authorizationRequestId`、`decision`
- `decision` 固定值为 `APPROVE`、`REJECT`
- `POST /oauth2/token` 固定支持 `grant_type=authorization_code` 和 `grant_type=refresh_token`
- `authorization_code` 换令牌请求固定包含 `grant_type`、`code`、`redirect_uri`、`client_id`、`client_secret`、`code_verifier`
- `refresh_token` 换令牌请求固定包含 `grant_type`、`refresh_token`、`client_id`、`client_secret`
- `POST /oauth2/introspect` 固定请求参数包含 `token`、`client_id`、`client_secret`
- `POST /oauth2/revoke` 固定请求参数包含 `token`、`client_id`、`client_secret`
- `GET /oauth2/userinfo` 固定使用 `OAuth2 access token` 读取当前授权用户信息
- 授权码换令牌成功响应固定使用 `OAuth2TokenResponse`
- 令牌校验成功响应固定使用 `OAuth2IntrospectionResponse`
- 用户信息读取成功响应固定使用 `OAuth2UserinfoResponse`
- 用户拒绝授权时，必须返回标准 `access_denied` 错误
- `OAuth2 refresh_token` 换发成功后，旧 `OAuth2 refresh_token` 不得再次使用

### 7.7 OAuthClient

- 维护第三方应用接入信息
- 维护 `clientId`、`clientSecret`、名称、授权范围、回调地址、状态
- 为授权服务提供只读查询能力

补充约束：

- `OAuthClient` 维护归属固定为 `Auth`
- `clientId` 全局唯一
- `clientSecret` 必须加密存储或哈希存储
- `redirectUris` 必须逐项完整登记
- 已停用 `OAuthClient` 不得参与授权流程
- 受保护资源范围固定为认证用户身份信息读取
- `OAuthClient` 必须支持维护联系人、备注、创建时间、更新时间

### 7.8 Permission Result Delivery

- 登录成功后不直接返回菜单树、权限码、数据权限上下文
- `Auth` 仅负责在认证阶段拉取并校验授权相关结果
- 前端如需页面导航与按钮权限，必须通过 `UPMS` 的只读接口读取

补充约束：

- `Auth` 不新增菜单树查询接口
- `Auth` 不新增权限码查询接口
- `Auth` 不新增数据权限上下文查询接口
- `Auth` 不作为权限结果缓存的唯一来源

### 7.9 Logout

- 用户可主动登出当前会话
- 登出后当前 `AuthSession` 状态改为 `LOGGED_OUT`
- 登出后当前 `access token` 和 `refresh token` 均不得再使用

补充约束：

- 请求头必须携带当前 `access token`
- 重复登出不得产生脏数据
- 登出操作必须记录审计日志

### 7.10 Self Password Change

- 当前登录用户可修改本人密码
- 必须校验旧密码
- 必须校验新密码复杂度
- 修改成功后失效全部活动会话

补充约束：

- 不允许绕过旧密码校验
- 请求体固定包含 `oldPassword`、`newPassword`
- 修改密码成功必须记录审计日志

### 7.11 Session Validation

- `Auth` 必须提供令牌校验能力
- 校验成功后返回稳定的会话上下文
- 校验失败时返回明确的失败状态

补充约束：

- 校验结果必须可被 `Gateway` 和其他服务复用
- 校验逻辑不得依赖权限数据
- 成功响应固定使用 `SessionValidationResponse`

### 7.12 Current Session Query

- 返回当前登录用户的基础会话信息
- 返回当前会话状态、登录方式、登录时间、最近访问时间

补充约束：

- 不返回密码、刷新令牌、第三方敏感凭据
- 成功响应固定使用 `CurrentSessionResponse`

### 7.13 Auth Audit Log

- 记录登录成功
- 记录登录失败
- 记录第三方应用授权成功和失败
- 记录令牌校验和撤销
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
- `clientId`
- 结果状态
- 失败原因摘要

补充约束：

- 审计日志必须持久化存储
- 审计日志必须可按 `tenantId`、`userId`、`identityType`、操作类型、结果状态、时间范围查询
- 审计日志不得记录明文密码、哈希值、短信验证码、用户登录态 `refresh token`、`OAuth2 access token`、`OAuth2 refresh token`
- 审计日志写入失败不得影响主业务提交结果

### 7.14 Cache

缓存命名约定：

- `auth:session:*`、`auth:refresh-token:*`、`auth:user-sessions:*`、`auth:tenant-sessions:*` 只用于用户登录态
- `auth:oauth-*` 只用于第三方应用 `OAuth2` 授权能力

缓存键：

- `auth:session:{sessionId}`
- `auth:refresh-token:{refreshTokenHash}`
- `auth:user-sessions:{tenantId}:{userId}`
- `auth:tenant-sessions:{tenantId}`
- `auth:oauth-client:{clientId}`
- `auth:oauth-code:{authorizationCode}`
- `auth:oauth-access-token:{accessTokenId}`
- `auth:oauth-refresh-token:{refreshTokenId}`

缓存内容：

- 会话缓存保存 `AuthSession` 核心状态
- 刷新令牌缓存保存 `refresh token` 哈希、会话绑定关系、过期时间和使用状态
- 用户会话索引缓存保存用户活动 `sessionId` 集合
- 租户会话索引缓存保存租户活动 `sessionId` 集合
- `OAuthClient` 缓存保存客户端接入配置
- 授权码缓存保存授权码绑定关系和过期时间
- `OAuth2 access token` 缓存保存令牌状态、客户端绑定关系、用户绑定关系和过期时间
- `OAuth2 refresh token` 缓存保存令牌状态、客户端绑定关系、用户绑定关系和过期时间
- 授权请求上下文缓存保存 `authorizationRequestId` 对应的授权上下文和使用状态

缓存时长：

- `auth:session:{sessionId}` 的 `TTL` 固定与会话剩余有效期一致
- `auth:refresh-token:{refreshTokenHash}` 的 `TTL` 固定为对应 `refresh token` 剩余有效期
- `auth:oauth-client:{clientId}` 的 `TTL` 固定为 `3600` 秒
- `auth:oauth-code:{authorizationCode}` 的 `TTL` 固定为 `300` 秒
- `auth:oauth-access-token:{accessTokenId}` 的 `TTL` 固定与对应第三方访问令牌剩余有效期一致
- `auth:oauth-refresh-token:{refreshTokenId}` 的 `TTL` 固定与对应第三方刷新令牌剩余有效期一致
- 授权请求上下文缓存 `TTL` 固定为 `300` 秒

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
- `OAuthClient` 配置变更
- `OAuthClient` 停用
- 授权码兑换成功
- 第三方访问令牌撤销
- 第三方刷新令牌换发
- 第三方刷新令牌撤销
- 授权请求上下文使用完成
- 授权请求上下文超时过期

补充约束：

- 缓存以主动失效为主，`TTL` 为兜底
- `refresh token` 只保存哈希值，不保存明文
- 会话缓存和刷新令牌缓存不得混合存储
- 第三方访问令牌缓存不得与用户登录会话缓存混合存储
- 第三方刷新令牌缓存不得与用户登录刷新令牌缓存混合存储
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

### 8.4 OAuth2 Authorization Code Flow

1. 第三方应用携带 `clientId`、`redirectUri`、`scope` 发起授权请求
2. `Auth` 校验 `OAuthClient` 状态和 `redirectUri`
3. `Auth` 生成 `authorizationRequestId` 并保存授权请求上下文
4. 用户完成登录并确认授权
5. `Auth` 通过 `POST /oauth2/authorize/decision` 基于 `authorizationRequestId` 接收授权确认结果
6. `Auth` 生成一次性授权码
7. 第三方应用使用授权码换取访问令牌和刷新令牌
8. 第三方应用后续使用访问令牌访问受保护接口

### 8.5 OAuth2 Token Introspection And Revocation

1. 第三方应用携带 `token`、`clientId`、`clientSecret` 发起令牌校验或撤销
2. `Auth` 校验 `OAuthClient` 身份与状态
3. `Auth` 校验目标令牌状态、归属和过期时间
4. `Auth` 返回令牌有效性结果或执行撤销
5. 撤销后的令牌后续不得继续使用

### 8.6 Permission Result Read

1. 用户登录成功
2. 前端调用 `UPMS` 菜单树、权限码、数据权限上下文只读接口
3. `UPMS` 根据 `tenantId`、`userId` 返回最新授权结果
4. 前端按返回结果渲染导航和按钮权限

### 8.7 OAuth2 Userinfo Read

1. 第三方应用携带第三方访问令牌访问 `GET /oauth2/userinfo`
2. `Auth` 校验令牌状态、归属、过期时间和授权范围
3. `Auth` 按已授予 `scope` 返回允许暴露的用户身份信息

### 8.8 Self Password Change

1. 当前登录用户提交旧密码和新密码
2. `Auth` 校验旧密码和新密码规则
3. `Auth` 调用 `UPMS` 完成密码持久化更新
4. `Auth` 失效该用户全部活动会话
5. 用户后续访问受保护资源时认证失败
6. 用户必须重新登录

### 8.9 User Or Tenant Invalidation

1. `UPMS` 或上游管理动作触发用户停用、用户删除、管理员改密或租户停用
2. `UPMS` 通过 `SessionCommandFacade` 调用 `Auth` 失效契约
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
| NFR-012 | OAuth2 Compatibility | 对第三方应用的授权服务必须遵循标准 `OAuth2` 语义 |
| NFR-013 | DTO Stability | 登录、会话校验、OAuth2 令牌相关 `DTO/VO` 字段必须保持向后兼容 |
| NFR-014 | Observability | 登录、发令牌、撤销、失效链路必须具备可追踪日志和审计记录 |
