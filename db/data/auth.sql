-- Execute after upms seed data so tenant_id/user_id/identity_id semantics stay aligned.
-- Default OAuth2 admin client:
--   client_id: bacon-admin-web
--   client_secret: BaconClient@123

INSERT INTO `bacon_auth_oauth_client` (
    `id`, `client_id`, `client_secret_hash`, `client_name`, `client_type`,
    `grant_types`, `scopes`, `redirect_uris`,
    `access_token_ttl_seconds`, `refresh_token_ttl_seconds`,
    `enabled`, `contact`, `remark`,
    `created_by`, `created_at`, `updated_by`, `updated_at`
) VALUES
(
    3000001, 'bacon-admin-web', '$2y$10$m.vDS3byljJ66FDH4l5EcOti3RW3YulgO53WkNmHnS9x/YXfvh.Pi', 'Bacon Admin Web', 'CONFIDENTIAL',
    JSON_ARRAY('authorization_code', 'refresh_token'),
    JSON_ARRAY('openid', 'profile', 'tenant.read', 'user.read'),
    JSON_ARRAY('http://127.0.0.1:5173/login/oauth2/code/bacon'),
    7200, 2592000,
    1, 'platform-admin@bacon.local', 'Seeded admin console OAuth2 client',
    '2000001', '2026-03-21 09:00:00.000', '2000001', '2026-03-21 09:00:00.000'
),
(
    3000002, 'demo-client', '$2y$10$NBPVngvyuzW/sX/.q8Q.KeQzz3ygG4edUCujU4w4U46YIxvLdu8YK', 'Demo OAuth Client', 'CONFIDENTIAL',
    JSON_ARRAY('authorization_code', 'refresh_token'),
    JSON_ARRAY('openid', 'profile'),
    JSON_ARRAY('http://127.0.0.1:3000/callback', 'http://127.0.0.1:8080/api/swagger-ui/oauth2-redirect.html'),
    1800, 2592000,
    1, 'dev@bacon.local', 'Seeded test-aligned OAuth2 client',
    '2000001', '2026-03-21 09:00:30.000', '2000001', '2026-03-21 09:00:30.000'
)
ON DUPLICATE KEY UPDATE
    `client_secret_hash` = VALUES(`client_secret_hash`),
    `client_name` = VALUES(`client_name`),
    `client_type` = VALUES(`client_type`),
    `grant_types` = VALUES(`grant_types`),
    `scopes` = VALUES(`scopes`),
    `redirect_uris` = VALUES(`redirect_uris`),
    `access_token_ttl_seconds` = VALUES(`access_token_ttl_seconds`),
    `refresh_token_ttl_seconds` = VALUES(`refresh_token_ttl_seconds`),
    `enabled` = VALUES(`enabled`),
    `contact` = VALUES(`contact`),
    `remark` = VALUES(`remark`),
    `updated_by` = VALUES(`updated_by`),
    `updated_at` = VALUES(`updated_at`);

INSERT INTO `bacon_auth_session` (
    `id`, `session_id`, `tenant_id`, `user_id`, `identity_id`,
    `identity_type`, `login_type`, `session_status`,
    `issued_at`, `last_access_time`, `expire_at`, `logout_at`, `invalidate_reason`
) VALUES (
    3000051, '8f5b16cf-94d2-4e8a-9d8d-bf41f9f10051', 'T1000001', '2000001', 2100001,
    'ACCOUNT', 'PASSWORD', 'ACTIVE',
    '2026-03-21 09:05:00.000', '2026-03-21 09:15:00.000', '2026-03-21 09:35:00.000', NULL, NULL
) ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `user_id` = VALUES(`user_id`),
    `identity_id` = VALUES(`identity_id`),
    `identity_type` = VALUES(`identity_type`),
    `login_type` = VALUES(`login_type`),
    `session_status` = VALUES(`session_status`),
    `issued_at` = VALUES(`issued_at`),
    `last_access_time` = VALUES(`last_access_time`),
    `expire_at` = VALUES(`expire_at`),
    `logout_at` = VALUES(`logout_at`),
    `invalidate_reason` = VALUES(`invalidate_reason`);

INSERT INTO `bacon_auth_refresh_token_session` (
    `id`, `session_id`, `refresh_token_hash`, `token_status`,
    `issued_at`, `expire_at`, `used_at`
) VALUES (
    3000052, '8f5b16cf-94d2-4e8a-9d8d-bf41f9f10051', 'mWjF1sE4wE3rM5s0e2YQnB7M3gW4M0i4hU3wT8JmA5I', 'ACTIVE',
    '2026-03-21 09:05:00.000', '2026-03-28 09:05:00.000', NULL
) ON DUPLICATE KEY UPDATE
    `session_id` = VALUES(`session_id`),
    `token_status` = VALUES(`token_status`),
    `issued_at` = VALUES(`issued_at`),
    `expire_at` = VALUES(`expire_at`),
    `used_at` = VALUES(`used_at`);

INSERT INTO `bacon_auth_oauth_consent` (
    `id`, `client_id`, `tenant_id`, `user_id`, `granted_scopes`, `granted_at`
) VALUES (
    3000101, 'bacon-admin-web', 'T1000001', '2000001',
    JSON_ARRAY('openid', 'profile', 'tenant.read', 'user.read'),
    '2026-03-21 09:10:00.000'
) ON DUPLICATE KEY UPDATE
    `granted_scopes` = VALUES(`granted_scopes`),
    `granted_at` = VALUES(`granted_at`);

INSERT INTO `bacon_auth_oauth_authorization_code` (
    `id`, `authorization_code`, `client_id`, `tenant_id`, `user_id`,
    `redirect_uri`, `scopes`, `code_challenge`, `code_challenge_method`,
    `issued_at`, `expire_at`, `used`
) VALUES (
    3000151, '4b9df6d6d82e4e6e8b4f5d3b9cb40151', 'bacon-admin-web', 'T1000001', '2000001',
    'http://127.0.0.1:5173/login/oauth2/code/bacon',
    JSON_ARRAY('openid', 'profile', 'tenant.read', 'user.read'),
    'pkce-challenge-sample-3000151', 'S256',
    '2026-03-21 09:11:00.000', '2026-03-21 09:16:00.000', 0
) ON DUPLICATE KEY UPDATE
    `client_id` = VALUES(`client_id`),
    `tenant_id` = VALUES(`tenant_id`),
    `user_id` = VALUES(`user_id`),
    `redirect_uri` = VALUES(`redirect_uri`),
    `scopes` = VALUES(`scopes`),
    `code_challenge` = VALUES(`code_challenge`),
    `code_challenge_method` = VALUES(`code_challenge_method`),
    `issued_at` = VALUES(`issued_at`),
    `expire_at` = VALUES(`expire_at`),
    `used` = VALUES(`used`);

INSERT INTO `bacon_auth_oauth_access_token` (
    `id`, `token_id`, `token_hash`, `client_id`, `tenant_id`, `user_id`,
    `scopes`, `issued_at`, `expire_at`, `token_status`
) VALUES (
    3000152, 'q7Y0Y8nY7-6w2s-McK2n2N4XG0nA1N3cQ5hV5k3f8M', 'q7Y0Y8nY7-6w2s-McK2n2N4XG0nA1N3cQ5hV5k3f8M', 'bacon-admin-web', 'T1000001', '2000001',
    JSON_ARRAY('openid', 'profile', 'tenant.read', 'user.read'),
    '2026-03-21 09:11:10.000', '2026-03-21 09:41:10.000', 'ACTIVE'
) ON DUPLICATE KEY UPDATE
    `token_hash` = VALUES(`token_hash`),
    `client_id` = VALUES(`client_id`),
    `tenant_id` = VALUES(`tenant_id`),
    `user_id` = VALUES(`user_id`),
    `scopes` = VALUES(`scopes`),
    `issued_at` = VALUES(`issued_at`),
    `expire_at` = VALUES(`expire_at`),
    `token_status` = VALUES(`token_status`);

INSERT INTO `bacon_auth_oauth_refresh_token` (
    `id`, `token_id`, `token_hash`, `access_token_id`, `client_id`, `tenant_id`, `user_id`,
    `issued_at`, `expire_at`, `token_status`
) VALUES (
    3000153, 'J2nA6vD4uM9pQ1kZ8rB5cT3wX7yN0fL2hS4gP6mR1q', 'J2nA6vD4uM9pQ1kZ8rB5cT3wX7yN0fL2hS4gP6mR1q', 'q7Y0Y8nY7-6w2s-McK2n2N4XG0nA1N3cQ5hV5k3f8M', 'bacon-admin-web', 'T1000001', '2000001',
    '2026-03-21 09:11:10.000', '2026-04-20 09:11:10.000', 'ACTIVE'
) ON DUPLICATE KEY UPDATE
    `token_hash` = VALUES(`token_hash`),
    `access_token_id` = VALUES(`access_token_id`),
    `client_id` = VALUES(`client_id`),
    `tenant_id` = VALUES(`tenant_id`),
    `user_id` = VALUES(`user_id`),
    `issued_at` = VALUES(`issued_at`),
    `expire_at` = VALUES(`expire_at`),
    `token_status` = VALUES(`token_status`);

INSERT INTO `bacon_auth_audit_log` (
    `id`, `tenant_id`, `user_id`, `identity_id`, `identity_type`,
    `session_id`, `client_id`, `action_type`, `result_status`,
    `failure_reason`, `request_ip`, `user_agent`, `occurred_at`
) VALUES (
    3000201, 'T1000001', '2000001', 2100001, 'ACCOUNT',
    NULL, 'bacon-admin-web', 'SEED_INIT', 'SUCCESS',
    NULL, '127.0.0.1', 'db-seed', '2026-03-21 09:15:00.000'
) ON DUPLICATE KEY UPDATE
    `action_type` = VALUES(`action_type`),
    `result_status` = VALUES(`result_status`),
    `failure_reason` = VALUES(`failure_reason`),
    `request_ip` = VALUES(`request_ip`),
    `user_agent` = VALUES(`user_agent`),
    `occurred_at` = VALUES(`occurred_at`);
