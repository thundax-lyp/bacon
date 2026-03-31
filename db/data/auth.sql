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
) VALUES (
    3000001, 'bacon-admin-web', '$2y$10$m.vDS3byljJ66FDH4l5EcOti3RW3YulgO53WkNmHnS9x/YXfvh.Pi', 'Bacon Admin Web', 'CONFIDENTIAL',
    JSON_ARRAY('authorization_code', 'refresh_token'),
    JSON_ARRAY('openid', 'profile', 'tenant.read', 'user.read'),
    JSON_ARRAY('http://127.0.0.1:5173/login/oauth2/code/bacon'),
    7200, 2592000,
    1, 'platform-admin@bacon.local', 'Seeded admin console OAuth2 client',
    '2000001', '2026-03-21 09:00:00.000', '2000001', '2026-03-21 09:00:00.000'
) ON DUPLICATE KEY UPDATE
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

INSERT INTO `bacon_auth_oauth_consent` (
    `id`, `client_id`, `tenant_id`, `user_id`, `granted_scopes`, `granted_at`
) VALUES (
    3000101, 'bacon-admin-web', 'T1000001', '2000001',
    JSON_ARRAY('openid', 'profile', 'tenant.read', 'user.read'),
    '2026-03-21 09:10:00.000'
) ON DUPLICATE KEY UPDATE
    `granted_scopes` = VALUES(`granted_scopes`),
    `granted_at` = VALUES(`granted_at`);

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
