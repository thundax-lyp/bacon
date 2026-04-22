CREATE TABLE IF NOT EXISTS `bacon_auth_session` (
    `id` bigint NOT NULL,
    `session_id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `identity_id` varchar(64) NOT NULL,
    `identity_type` varchar(16) NOT NULL,
    `login_type` varchar(16) NOT NULL,
    `session_status` varchar(16) NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `last_access_time` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `logout_at` datetime(3) DEFAULT NULL,
    `invalidate_reason` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_tenant_user_status` (`tenant_id`, `user_id`, `session_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_auth_refresh_token_session` (
    `id` bigint NOT NULL,
    `session_id` varchar(64) NOT NULL,
    `refresh_token_hash` varchar(255) NOT NULL,
    `token_status` varchar(16) NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `used_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_token_hash` (`refresh_token_hash`),
    KEY `idx_session_status` (`session_id`, `token_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_auth_oauth_client` (
    `id` bigint NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `client_secret_hash` varchar(255) NOT NULL,
    `client_name` varchar(128) NOT NULL,
    `client_type` varchar(16) NOT NULL,
    `grant_types` json NOT NULL,
    `scopes` json NOT NULL,
    `redirect_uris` json NOT NULL,
    `access_token_ttl_seconds` bigint NOT NULL,
    `refresh_token_ttl_seconds` bigint NOT NULL,
    `enabled` tinyint(1) NOT NULL,
    `contact` varchar(128) DEFAULT NULL,
    `remark` varchar(255) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_auth_oauth_authorization_code` (
    `id` bigint NOT NULL,
    `authorization_code` varchar(128) NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `redirect_uri` varchar(512) NOT NULL,
    `scopes` json NOT NULL,
    `code_challenge` varchar(128) NOT NULL,
    `code_challenge_method` varchar(16) NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `used` tinyint(1) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_authorization_code` (`authorization_code`),
    KEY `idx_client_user_expire` (`client_id`, `user_id`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_auth_oauth_access_token` (
    `id` bigint NOT NULL,
    `token_id` varchar(64) NOT NULL,
    `token_hash` varchar(255) NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `scopes` json NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `token_status` varchar(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_id` (`token_id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    KEY `idx_client_user_status` (`client_id`, `user_id`, `token_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_auth_oauth_refresh_token` (
    `id` bigint NOT NULL,
    `token_id` varchar(64) NOT NULL,
    `token_hash` varchar(255) NOT NULL,
    `access_token_id` varchar(64) NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `token_status` varchar(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_id` (`token_id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    KEY `idx_client_user_status` (`client_id`, `user_id`, `token_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_auth_oauth_consent` (
    `id` bigint NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `granted_scopes` json NOT NULL,
    `granted_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_user` (`client_id`, `tenant_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_auth_audit_log` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) DEFAULT NULL,
    `identity_id` varchar(64) DEFAULT NULL,
    `identity_type` varchar(16) DEFAULT NULL,
    `session_id` varchar(64) DEFAULT NULL,
    `client_id` varchar(64) DEFAULT NULL,
    `action_type` varchar(64) NOT NULL,
    `result_status` varchar(32) NOT NULL,
    `failure_reason` varchar(255) DEFAULT NULL,
    `request_ip` varchar(64) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_occurred` (`tenant_id`, `occurred_at`),
    KEY `idx_user_occurred` (`user_id`, `occurred_at`),
    KEY `idx_client_occurred` (`client_id`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
