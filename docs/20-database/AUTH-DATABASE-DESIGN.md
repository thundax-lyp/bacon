# AUTH DATABASE DESIGN

## 1. Purpose

本文档定义 `Auth` 业务域的数据库设计。  
本文档可直接用于生成 `DDL`、`DataObject`、`Mapper`、`Repository` 与查询实现。  
本文档只定义 `Auth` 自有的持久化对象、字段、索引、关联和查询模型，不重复 `Auth` 业务需求文档中的流程描述。  
本文档必须遵守 [DATABASE-RULES.md](../00-governance/DATABASE-RULES.md)。如与工程级数据库规范冲突，以 [DATABASE-RULES.md](../00-governance/DATABASE-RULES.md) 为准。

## 2. Scope

本文档定义以下持久化对象：
- `AuthSession`
- `RefreshTokenSession`
- `OAuthClient`
- `OAuthAuthorizationCode`
- `OAuthAccessToken`
- `OAuthRefreshToken`
- `OAuthConsent`
- `AuthAuditLog`

本文档不定义以下持久化对象：
- `AccessTokenClaims`
- `LoginRequest`
- `LoginResult`
- `AuthPrincipal`
- `SmsCaptcha`
- `ThirdPartyIdentityBinding`
- OAuth2 授权请求上下文缓存

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库实例可用的 `utf8mb4` 排序规则（推荐 `utf8mb4_unicode_ci`）
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- `tenant_id` 固定承载 `TenantId` 文本值，数据库类型固定使用 `varchar(64)`
- `user_id` 及所有直接承载 `UserId` 的字段固定使用 `varchar(64)`
- 布尔字段统一使用 `tinyint(1)`
- 枚举字段统一使用 `varchar`
- `Auth` 运行态数据按租户隔离；凡是租户相关运行态数据必须包含 `tenant_id`
- `Auth` 不使用逻辑删除字段
- 会话表、令牌表、授权码表、授权同意表、审计表使用领域时间字段，不重复增加 `created_at`、`updated_at`
- 只有后台维护的主数据表和配置表增加 `created_at`、`created_by`、`updated_at`、`updated_by`
- 高频查询索引固定围绕业务键、租户、状态、时间字段设计

## 4. Naming Rules

- 表名前缀固定使用 `bacon_auth_`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`
- 其他时间字段使用业务语义命名，例如 `issued_at`、`expire_at`、`logout_at`、`granted_at`、`used_at`

## 5. Enum Storage Rules

### 5.1 Fixed Enums

- `identity_type`: `ACCOUNT`、`EMAIL`、`PHONE`、`WECOM`、`GITHUB`
- `login_type`: `PASSWORD`、`SMS`、`WECOM`、`GITHUB`
- `session_status`: `ACTIVE`、`LOGGED_OUT`、`INVALIDATED`、`EXPIRED`
- `token_status`: `ACTIVE`、`USED`、`INVALIDATED`、`EXPIRED`
- `oauth_token_status`: `ACTIVE`、`USED`、`REVOKED`、`EXPIRED`
- `client_type`: `CONFIDENTIAL`
- `decision`: `APPROVE`、`REJECT`

### 5.2 Fixed Length Rules

- `session_id`: `varchar(64)`
- `tenant_id`: `varchar(64)`
- `client_id`: `varchar(64)`
- `token_id`: `varchar(64)`
- `authorization_code`: `varchar(128)`
- `client_name`: `varchar(128)`
- `client_secret_hash`: `varchar(255)`
- `redirect_uri`: `varchar(512)`
- `contact`: `varchar(128)`
- `remark`: `varchar(255)`
- `code_challenge`: `varchar(128)`
- `code_challenge_method`: `varchar(16)`
- `refresh_token_hash`: `varchar(255)`
- `token_hash`: `varchar(255)`
- `invalidate_reason`: `varchar(64)`
- `failure_reason`: `varchar(255)`
- `request_ip`: `varchar(64)`
- `user_agent`: `varchar(512)`
- `action_type`: `varchar(64)`
- `result_status`: `varchar(32)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `AuthSession` | `bacon_auth_session` |
| `RefreshTokenSession` | `bacon_auth_refresh_token_session` |
| `OAuthClient` | `bacon_auth_oauth_client` |
| `OAuthAuthorizationCode` | `bacon_auth_oauth_authorization_code` |
| `OAuthAccessToken` | `bacon_auth_oauth_access_token` |
| `OAuthRefreshToken` | `bacon_auth_oauth_refresh_token` |
| `OAuthConsent` | `bacon_auth_oauth_consent` |
| `AuthAuditLog` | `bacon_auth_audit_log` |

## 7. Table Design

### 7.1 `bacon_auth_session`

表类型：`Runtime Table`

用途：

- 持久化登录会话
- 承载当前会话状态、签发信息、过期信息和失效信息

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `session_id` | `varchar(64)` | N | 会话业务键，全局唯一 |
| `tenant_id` | `varchar(64)` | N | 租户业务键，承载 `TenantId` 文本值 |
| `user_id` | `varchar(64)` | N | 用户主键，承载 `UserId` 文本值 |
| `identity_id` | `varchar(64)` | N | 身份主键，承载 `UserIdentityId` 文本值 |
| `identity_type` | `varchar(16)` | N | 身份类型，取值见 `identity_type` |
| `login_type` | `varchar(16)` | N | 登录方式，取值见 `login_type` |
| `session_status` | `varchar(16)` | N | 会话状态，取值见 `session_status` |
| `issued_at` | `datetime(3)` | N | 会话签发时间 |
| `last_access_time` | `datetime(3)` | N | 最近访问时间 |
| `expire_at` | `datetime(3)` | N | 会话过期时间 |
| `logout_at` | `datetime(3)` | Y | 主动登出时间 |
| `invalidate_reason` | `varchar(64)` | Y | 被动失效原因摘要 |

索引与约束：

- `pk(id)`
- `uk_session_id(session_id)`
- `idx_tenant_user_status(tenant_id, user_id, session_status)`

### 7.2 `bacon_auth_refresh_token_session`

表类型：`Runtime Table`

用途：

- 持久化面向用户登录会话的刷新令牌
- 只保存刷新令牌哈希，不保存刷新令牌明文

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `session_id` | `varchar(64)` | N | 所属会话业务键，关联 `bacon_auth_session.session_id` |
| `refresh_token_hash` | `varchar(255)` | N | 刷新令牌哈希，全局唯一 |
| `token_status` | `varchar(16)` | N | 刷新令牌状态，取值见 `token_status` |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `used_at` | `datetime(3)` | Y | 成功使用时间 |

索引与约束：

- `pk(id)`
- `uk_refresh_token_hash(refresh_token_hash)`
- `idx_session_status(session_id, token_status)`

### 7.3 `bacon_auth_oauth_client`

表类型：`Master Table`

用途：

- 持久化 OAuth2 客户端主数据
- 由后台管理维护

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `client_id` | `varchar(64)` | N | 客户端标识，全局唯一 |
| `client_secret_hash` | `varchar(255)` | N | 客户端密钥哈希，不保存明文 |
| `client_name` | `varchar(128)` | N | 客户端名称 |
| `client_type` | `varchar(16)` | N | 固定为 `CONFIDENTIAL` |
| `grant_types` | `json` | N | 授权方式集合，保存字符串数组 |
| `scopes` | `json` | N | 客户端允许申请的范围集合，保存字符串数组 |
| `redirect_uris` | `json` | N | 回调地址集合，保存字符串数组 |
| `access_token_ttl_seconds` | `int` | N | 访问令牌 TTL，单位秒 |
| `refresh_token_ttl_seconds` | `int` | N | 刷新令牌 TTL，单位秒 |
| `enabled` | `tinyint(1)` | N | 是否启用 |
| `contact` | `varchar(128)` | Y | 联系人 |
| `remark` | `varchar(255)` | Y | 备注 |
| `created_by` | `varchar(64)` | Y | 创建人用户主键，承载 `UserId` 文本值 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人用户主键，承载 `UserId` 文本值 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_client_id(client_id)`

### 7.4 `bacon_auth_oauth_authorization_code`

表类型：`Runtime Table`

用途：

- 持久化 OAuth2 授权码
- 授权码只允许成功兑换一次

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `authorization_code` | `varchar(128)` | N | 授权码，全局唯一 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键，承载 `TenantId` 文本值 |
| `user_id` | `varchar(64)` | N | 用户主键，承载 `UserId` 文本值 |
| `redirect_uri` | `varchar(512)` | N | 本次授权使用的回调地址 |
| `scopes` | `json` | N | 本次授权通过的范围集合，保存字符串数组 |
| `code_challenge` | `varchar(128)` | N | PKCE `code_challenge` |
| `code_challenge_method` | `varchar(16)` | N | 固定为 `S256` |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `used` | `tinyint(1)` | N | 是否已成功兑换 |

索引与约束：

- `pk(id)`
- `uk_authorization_code(authorization_code)`
- `idx_client_user_expire(client_id, user_id, expire_at)`

固定说明：

- 授权码使用 `used` 表达是否已兑换，不使用 `token_status`

### 7.5 `bacon_auth_oauth_access_token`

表类型：`Runtime Table`

用途：

- 持久化 OAuth2 访问令牌
- 只保存访问令牌业务键与哈希，不保存访问令牌明文

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `token_id` | `varchar(64)` | N | 访问令牌业务键，全局唯一 |
| `token_hash` | `varchar(255)` | N | 访问令牌哈希，全局唯一 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键，承载 `TenantId` 文本值 |
| `user_id` | `varchar(64)` | N | 用户主键，承载 `UserId` 文本值 |
| `scopes` | `json` | N | 本令牌范围集合，保存字符串数组 |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `token_status` | `varchar(16)` | N | 令牌状态，取值见 `oauth_token_status` |

索引与约束：

- `pk(id)`
- `uk_token_id(token_id)`
- `uk_token_hash(token_hash)`
- `idx_client_user_status(client_id, user_id, token_status)`

### 7.6 `bacon_auth_oauth_refresh_token`

表类型：`Runtime Table`

用途：

- 持久化 OAuth2 刷新令牌
- 用于刷新访问令牌时做校验、状态判断和关联定位

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `token_id` | `varchar(64)` | N | 刷新令牌业务键，全局唯一 |
| `token_hash` | `varchar(255)` | N | 刷新令牌哈希，全局唯一 |
| `access_token_id` | `varchar(64)` | N | 对应访问令牌业务键，关联 `bacon_auth_oauth_access_token.token_id` |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键，承载 `TenantId` 文本值 |
| `user_id` | `varchar(64)` | N | 用户主键，承载 `UserId` 文本值 |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `token_status` | `varchar(16)` | N | 令牌状态，取值见 `oauth_token_status` |

索引与约束：

- `pk(id)`
- `uk_token_id(token_id)`
- `uk_token_hash(token_hash)`
- `idx_client_user_status(client_id, user_id, token_status)`

### 7.7 `bacon_auth_oauth_consent`

表类型：`Runtime Table`

用途：

- 持久化用户对 OAuth2 客户端的授权同意结果
- 每个 `client_id + tenant_id + user_id` 只保留一条当前授权同意记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键，承载 `TenantId` 文本值 |
| `user_id` | `varchar(64)` | N | 用户主键，承载 `UserId` 文本值 |
| `granted_scopes` | `json` | N | 已同意的范围集合，保存字符串数组 |
| `granted_at` | `datetime(3)` | N | 授权时间 |

索引与约束：

- `pk(id)`
- `uk_client_user(client_id, tenant_id, user_id)`

### 7.8 `bacon_auth_audit_log`

表类型：`Audit Log Table`

用途：

- 记录登录、登出、令牌处理、OAuth2 授权相关的审计事件
- 只追加，不更新历史记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键，承载 `TenantId` 文本值 |
| `user_id` | `varchar(64)` | Y | 用户主键；匿名或未定位用户时可为空，承载 `UserId` 文本值 |
| `identity_id` | `varchar(64)` | Y | 身份主键，承载 `UserIdentityId` 文本值 |
| `identity_type` | `varchar(16)` | Y | 身份类型 |
| `session_id` | `varchar(64)` | Y | 关联会话业务键 |
| `client_id` | `varchar(64)` | Y | 关联 OAuth2 客户端标识 |
| `action_type` | `varchar(64)` | N | 审计动作类型 |
| `result_status` | `varchar(32)` | N | 审计结果状态 |
| `failure_reason` | `varchar(255)` | Y | 失败原因摘要，不记录敏感明文 |
| `request_ip` | `varchar(64)` | Y | 请求来源 IP |
| `user_agent` | `varchar(512)` | Y | 请求 User-Agent |
| `occurred_at` | `datetime(3)` | N | 事件发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_user_occurred(user_id, occurred_at)`
- `idx_client_occurred(client_id, occurred_at)`

## 8. Relationship Rules

- `bacon_auth_refresh_token_session.session_id` 关联 `bacon_auth_session.session_id`
- `bacon_auth_oauth_refresh_token.access_token_id` 关联 `bacon_auth_oauth_access_token.token_id`
- 当前设计不强制数据库外键
- 关联一致性由应用层和领域层保证
- `OAuthClient` 是 `Auth` 自有主数据
- `tenant_id`、`user_id`、`identity_id` 的实际合法性由跨域调用 `UPMS` 校验

## 9. Persistence Rules

- `AuthSession.session_id` 全局唯一
- `RefreshTokenSession.refresh_token_hash` 全局唯一
- `OAuthClient.client_id` 全局唯一
- `OAuthAuthorizationCode.authorization_code` 全局唯一
- `OAuthAccessToken.token_id` 全局唯一
- `OAuthAccessToken.token_hash` 全局唯一
- `OAuthRefreshToken.token_id` 全局唯一
- `OAuthRefreshToken.token_hash` 全局唯一
- `OAuthConsent` 以 `(client_id, tenant_id, user_id)` 表达唯一授权同意记录
- `refresh_token_hash`、`token_hash`、`client_secret_hash` 只保存哈希值，不保存明文
- `grant_types`、`scopes`、`redirect_uris`、`granted_scopes` 使用 `json` 保存字符串数组；这些字段不作为高频过滤主条件
- `SmsCaptcha` 固定只走缓存，不建表
- OAuth2 授权请求上下文固定只走缓存，不建表
- `AccessTokenClaims` 是 JWT 载荷，不建表
- `ThirdPartyIdentityBinding` 不单独建表，第三方身份绑定由 `UPMS UserIdentity` 承载

## 10. Query Model Rules

- 当前会话读取与会话状态查询主表固定为 `bacon_auth_session`
- 基于刷新令牌的会话校验主表固定为 `bacon_auth_refresh_token_session`
- OAuth2 客户端配置读取主表固定为 `bacon_auth_oauth_client`
- OAuth2 授权码兑换主表固定为 `bacon_auth_oauth_authorization_code`
- OAuth2 访问令牌校验主表固定为 `bacon_auth_oauth_access_token`
- OAuth2 刷新令牌换发主表固定为 `bacon_auth_oauth_refresh_token`
- OAuth2 授权同意查询主表固定为 `bacon_auth_oauth_consent`
- 审计查询主表固定为 `bacon_auth_audit_log`
