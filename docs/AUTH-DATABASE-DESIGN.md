# AUTH DATABASE DESIGN

## 1. Purpose

本文档定义 `Auth` 的数据结构与数据库设计。  
目标是让 AI 可直接据此生成表结构、实体、`Mapper`、`Repository` 和查询实现。  
本文档只保留数据库设计所需的稳定信息，不重复业务需求文档中的冗余描述。
本文档必须遵守 [DATABASE-REQUIREMENTS.md](DATABASE-REQUIREMENTS.md)。
如与工程级数据库规范冲突，以 [DATABASE-REQUIREMENTS.md](./DATABASE-REQUIREMENTS.md) 为准。

## 2. Scope

当前范围覆盖：

- `AuthSession`
- `RefreshTokenSession`
- `OAuthClient`
- `OAuthAuthorizationCode`
- `OAuthAccessToken`
- `OAuthRefreshToken`
- `OAuthConsent`
- `AuthAuditLog`

不覆盖：

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
- 排序规则固定使用 `utf8mb4_0900_ai_ci`
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 业务布尔字段统一使用 `tinyint(1)`
- 枚举字段统一使用 `varchar`
- 所有跨租户运行态数据必须包含 `tenant_id`
- 所有查询高频索引必须围绕业务键与状态字段建立

## 3.1 Common Field Rules

- 后台维护的客户端配置表统一包含 `created_at`、`created_by`、`updated_at`、`updated_by`
- 会话表、令牌表、授权码表、授权同意表、审计表使用领域时间字段，不额外重复声明通用时间字段
- `Auth` 当前范围内不引入逻辑删除字段

## 4. Naming Rules

- 表名固定格式使用 `bacon_${domain}_${model}`
- `Auth` 表名统一使用 `bacon_auth_` 前缀
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 审计发生时间统一命名为 `occurred_at`

## 5. Enum Storage Rules

- `identity_type`: `ACCOUNT`、`PHONE`、`WECOM`、`GITHUB`
- `login_type`: `PASSWORD`、`SMS`、`WECOM`、`GITHUB`
- `session_status`: `ACTIVE`、`LOGGED_OUT`、`INVALIDATED`、`EXPIRED`
- `token_status`: `ACTIVE`、`USED`、`INVALIDATED`、`EXPIRED`
- `oauth_token_status`: `ACTIVE`、`USED`、`REVOKED`、`EXPIRED`
- `client_type`: `CONFIDENTIAL`
- `decision`: `APPROVE`、`REJECT`

## 5.1 Length Rules

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

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `session_id` | `varchar(64)` | N | 会话业务键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `identity_id` | `bigint` | N | 身份主键 |
| `identity_type` | `varchar(16)` | N | 身份类型 |
| `login_type` | `varchar(16)` | N | 登录方式 |
| `session_status` | `varchar(16)` | N | 会话状态 |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `last_access_time` | `datetime(3)` | N | 最后访问时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `logout_at` | `datetime(3)` | Y | 登出时间 |
| `invalidate_reason` | `varchar(64)` | Y | 失效原因 |

索引与约束：

- `pk(id)`
- `uk_session_id(session_id)`
- `idx_tenant_user_status(tenant_id, user_id, session_status)`

### 7.2 `bacon_auth_refresh_token_session`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `session_id` | `varchar(64)` | N | 会话业务键 |
| `refresh_token_hash` | `varchar(255)` | N | 刷新令牌哈希 |
| `token_status` | `varchar(16)` | N | 令牌状态 |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `used_at` | `datetime(3)` | Y | 使用时间 |

索引与约束：

- `pk(id)`
- `uk_refresh_token_hash(refresh_token_hash)`
- `idx_session_status(session_id, token_status)`

### 7.3 `bacon_auth_oauth_client`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `client_secret_hash` | `varchar(255)` | N | 客户端密钥哈希 |
| `client_name` | `varchar(128)` | N | 客户端名称 |
| `client_type` | `varchar(16)` | N | 固定为 `CONFIDENTIAL` |
| `grant_types` | `json` | N | 授权类型集合 |
| `scopes` | `json` | N | 范围集合 |
| `redirect_uris` | `json` | N | 回调地址集合 |
| `access_token_ttl_seconds` | `int` | N | OAuth2 访问令牌 TTL |
| `refresh_token_ttl_seconds` | `int` | N | OAuth2 刷新令牌 TTL |
| `enabled` | `tinyint(1)` | N | 是否启用 |
| `contact` | `varchar(128)` | Y | 联系人 |
| `remark` | `varchar(255)` | Y | 备注 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_client_id(client_id)`

### 7.4 `bacon_auth_oauth_authorization_code`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `authorization_code` | `varchar(128)` | N | 授权码 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `redirect_uri` | `varchar(512)` | N | 回调地址 |
| `scopes` | `json` | N | 已授权范围集合 |
| `code_challenge` | `varchar(128)` | N | PKCE challenge |
| `code_challenge_method` | `varchar(16)` | N | 固定 `S256` |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `used` | `tinyint(1)` | N | 是否已使用 |

索引与约束：

- `pk(id)`
- `uk_authorization_code(authorization_code)`
- `idx_client_user_expire(client_id, user_id, expire_at)`

### 7.5 `bacon_auth_oauth_access_token`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `token_id` | `varchar(64)` | N | 访问令牌业务键 |
| `token_hash` | `varchar(255)` | N | 访问令牌哈希 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `scopes` | `json` | N | 范围集合 |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `token_status` | `varchar(16)` | N | 令牌状态 |

索引与约束：

- `pk(id)`
- `uk_token_id(token_id)`
- `uk_token_hash(token_hash)`
- `idx_client_user_status(client_id, user_id, token_status)`

### 7.6 `bacon_auth_oauth_refresh_token`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `token_id` | `varchar(64)` | N | 刷新令牌业务键 |
| `token_hash` | `varchar(255)` | N | 刷新令牌哈希 |
| `access_token_id` | `varchar(64)` | N | 访问令牌业务键 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `issued_at` | `datetime(3)` | N | 签发时间 |
| `expire_at` | `datetime(3)` | N | 过期时间 |
| `token_status` | `varchar(16)` | N | 令牌状态 |

索引与约束：

- `pk(id)`
- `uk_token_id(token_id)`
- `uk_token_hash(token_hash)`
- `idx_client_user_status(client_id, user_id, token_status)`

### 7.7 `bacon_auth_oauth_consent`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `client_id` | `varchar(64)` | N | 客户端标识 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | N | 用户主键 |
| `granted_scopes` | `json` | N | 已授权范围集合 |
| `granted_at` | `datetime(3)` | N | 授权时间 |

索引与约束：

- `pk(id)`
- `uk_client_user(client_id, tenant_id, user_id)`

### 7.8 `bacon_auth_audit_log`

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | N | 租户业务键 |
| `user_id` | `bigint` | Y | 用户主键 |
| `identity_id` | `bigint` | Y | 身份主键 |
| `identity_type` | `varchar(16)` | Y | 身份类型 |
| `session_id` | `varchar(64)` | Y | 会话业务键 |
| `client_id` | `varchar(64)` | Y | 客户端标识 |
| `action_type` | `varchar(64)` | N | 操作类型 |
| `result_status` | `varchar(32)` | N | 结果状态 |
| `failure_reason` | `varchar(255)` | Y | 失败原因摘要 |
| `request_ip` | `varchar(64)` | Y | 客户端 IP |
| `user_agent` | `varchar(512)` | Y | User-Agent |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_user_occurred(user_id, occurred_at)`
- `idx_client_occurred(client_id, occurred_at)`

## 8. Relationship Rules

- `bacon_auth_refresh_token_session.session_id -> bacon_auth_session.session_id`
- `bacon_auth_oauth_refresh_token.access_token_id -> bacon_auth_oauth_access_token.token_id`

固定约束：

- 当前设计不强制数据库外键
- `OAuthClient` 为 `Auth` 自有主数据
- `tenant_id`、`user_id`、`identity_id` 的实际合法性由跨域调用 `UPMS` 校验

## 9. Persistence Rules

- `AuthSession.session_id` 全局唯一
- `RefreshTokenSession.refresh_token_hash` 全局唯一
- `OAuthClient.client_id` 全局唯一
- `OAuthAuthorizationCode.authorization_code` 全局唯一
- `OAuthAccessToken.token_id`、`token_hash` 全局唯一
- `OAuthRefreshToken.token_id`、`token_hash` 全局唯一
- `refresh_token_hash`、`token_hash`、`client_secret_hash` 只保存哈希，不保存明文
- `SmsCaptcha`、授权请求上下文固定走缓存，不建表
- `AccessTokenClaims` 是 JWT 载荷，不建表
- `ThirdPartyIdentityBinding` 当前范围不单独建表，第三方身份绑定由 `UPMS UserIdentity` 承载

## 10. Query Model Rules

- 当前会话查询主表为 `bacon_auth_session`
- 用户刷新令牌校验主表为 `bacon_auth_refresh_token_session`
- OAuth2 客户端查询主表为 `bacon_auth_oauth_client`
- OAuth2 授权码兑换主表为 `bacon_auth_oauth_authorization_code`
- OAuth2 访问令牌校验主表为 `bacon_auth_oauth_access_token`
- OAuth2 刷新令牌换发主表为 `bacon_auth_oauth_refresh_token`
- OAuth2 授权同意查询主表为 `bacon_auth_oauth_consent`
- 审计查询主表为 `bacon_auth_audit_log`

## 11. Cache Mapping Rules

- `auth:session:{sessionId}` 来源于 `bacon_auth_session`
- `auth:refresh-token:{refreshTokenHash}` 来源于 `bacon_auth_refresh_token_session`
- `auth:oauth-client:{clientId}` 来源于 `bacon_auth_oauth_client`
- `auth:oauth-code:{authorizationCode}` 来源于 `bacon_auth_oauth_authorization_code`
- `auth:oauth-access-token:{accessTokenId}` 来源于 `bacon_auth_oauth_access_token`
- `auth:oauth-refresh-token:{refreshTokenId}` 来源于 `bacon_auth_oauth_refresh_token`

## 12. DDL Generation Notes

- `grant_types`、`scopes`、`redirect_uris`、`granted_scopes` 固定使用 JSON
- 审计日志不保存明文密码、短信验证码、明文令牌、明文客户端密钥
- 运行态表优先使用业务键索引，不依赖自增主键做业务查询

## 13. Open Items

无
