CREATE TABLE IF NOT EXISTS `bacon_upms_tenant` (
    `tenant_id` varchar(64) NOT NULL,
    `code` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `status` varchar(16) NOT NULL,
    `expired_at` datetime(3) DEFAULT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`tenant_id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_user` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `avatar_object_id` bigint DEFAULT NULL,
    `department_id` varchar(64) DEFAULT NULL,
    `status` varchar(16) NOT NULL,
    `deleted` tinyint(1) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_department_status` (`tenant_id`, `department_id`, `status`),
    KEY `idx_avatar_object_id` (`avatar_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_user_identity` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `identity_type` varchar(16) NOT NULL,
    `identity_value` varchar(255) NOT NULL,
    `enabled` tinyint(1) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity` (`tenant_id`, `identity_type`, `identity_value`),
    KEY `idx_tenant_user` (`tenant_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_user_credential` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `identity_id` varchar(64) DEFAULT NULL,
    `credential_type` varchar(32) NOT NULL,
    `factor_level` varchar(16) NOT NULL,
    `credential_value` varchar(1024) NOT NULL,
    `status` varchar(16) NOT NULL,
    `need_change_password` tinyint(1) NOT NULL,
    `failed_count` int NOT NULL,
    `failed_limit` int NOT NULL,
    `lock_reason` varchar(64) DEFAULT NULL,
    `locked_until` datetime(3) DEFAULT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `last_verified_at` datetime(3) DEFAULT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity_credential` (`identity_id`, `credential_type`),
    KEY `idx_tenant_user_status` (`tenant_id`, `user_id`, `status`),
    KEY `idx_tenant_identity_factor` (`tenant_id`, `identity_id`, `factor_level`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_department` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `code` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `parent_id` varchar(64) DEFAULT NULL,
    `leader_user_id` varchar(64) DEFAULT NULL,
    `status` varchar(16) NOT NULL,
    `deleted` tinyint(1) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`),
    KEY `idx_tenant_parent_status` (`tenant_id`, `parent_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_post` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `code` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `department_id` varchar(64) DEFAULT NULL,
    `sort` int NOT NULL,
    `status` varchar(16) NOT NULL,
    `deleted` tinyint(1) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_role` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `code` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `role_type` varchar(32) NOT NULL,
    `data_scope_type` varchar(32) NOT NULL,
    `status` varchar(16) NOT NULL,
    `built_in` tinyint(1) NOT NULL,
    `deleted` tinyint(1) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_tenant_role_type_status` (`tenant_id`, `role_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_menu` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `menu_type` varchar(16) NOT NULL,
    `name` varchar(128) NOT NULL,
    `parent_id` varchar(64) DEFAULT NULL,
    `route_path` varchar(255) DEFAULT NULL,
    `component_name` varchar(255) DEFAULT NULL,
    `icon` varchar(128) DEFAULT NULL,
    `sort` int NOT NULL,
    `visible` tinyint(1) NOT NULL,
    `status` varchar(16) NOT NULL,
    `permission_code` varchar(128) NOT NULL,
    `built_in` tinyint(1) NOT NULL,
    `deleted` tinyint(1) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_tenant_parent_status_visible` (`tenant_id`, `parent_id`, `status`, `visible`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_resource` (
    `id` varchar(64) NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `code` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `resource_type` varchar(16) NOT NULL,
    `module` varchar(64) NOT NULL,
    `path` varchar(255) NOT NULL,
    `method` varchar(16) NOT NULL,
    `status` varchar(16) NOT NULL,
    `permission_code` varchar(128) NOT NULL,
    `built_in` tinyint(1) NOT NULL,
    `deleted` tinyint(1) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    UNIQUE KEY `uk_path_method` (`path`, `method`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_tenant_resource_type_status` (`tenant_id`, `resource_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_user_role_rel` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `role_id` varchar(64) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`tenant_id`, `user_id`, `role_id`),
    KEY `idx_tenant_user` (`tenant_id`, `user_id`),
    KEY `idx_tenant_role` (`tenant_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_user_post_rel` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `post_id` varchar(64) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`tenant_id`, `user_id`, `post_id`),
    KEY `idx_tenant_user` (`tenant_id`, `user_id`),
    KEY `idx_tenant_post` (`tenant_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_role_menu_rel` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `role_id` varchar(64) NOT NULL,
    `menu_id` varchar(64) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`tenant_id`, `role_id`, `menu_id`),
    KEY `idx_tenant_role` (`tenant_id`, `role_id`),
    KEY `idx_tenant_menu` (`tenant_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_role_resource_rel` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `role_id` varchar(64) NOT NULL,
    `resource_id` varchar(64) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_resource` (`tenant_id`, `role_id`, `resource_id`),
    KEY `idx_tenant_role` (`tenant_id`, `role_id`),
    KEY `idx_tenant_resource` (`tenant_id`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_data_permission_rule` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `role_id` varchar(64) NOT NULL,
    `data_scope_type` varchar(32) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role` (`tenant_id`, `role_id`),
    KEY `idx_tenant_role` (`tenant_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_role_data_scope_rel` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `role_id` varchar(64) NOT NULL,
    `department_id` varchar(64) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_department` (`tenant_id`, `role_id`, `department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_audit_log` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) NOT NULL,
    `operator_id` varchar(64) DEFAULT NULL,
    `object_type` varchar(64) NOT NULL,
    `object_id` varchar(64) NOT NULL,
    `action_type` varchar(64) NOT NULL,
    `before_summary` json DEFAULT NULL,
    `after_summary` json DEFAULT NULL,
    `request_source` varchar(64) DEFAULT NULL,
    `result_status` varchar(32) NOT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_occurred` (`tenant_id`, `occurred_at`),
    KEY `idx_object` (`object_type`, `object_id`),
    KEY `idx_operator` (`operator_id`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_upms_sys_log` (
    `id` bigint NOT NULL,
    `tenant_id` varchar(64) DEFAULT NULL,
    `trace_id` varchar(64) NOT NULL,
    `request_id` varchar(64) NOT NULL,
    `module` varchar(64) NOT NULL,
    `action` varchar(128) NOT NULL,
    `event_type` varchar(32) NOT NULL,
    `result` varchar(32) NOT NULL,
    `operator_id` varchar(64) DEFAULT NULL,
    `operator_name` varchar(64) DEFAULT NULL,
    `client_ip` varchar(64) DEFAULT NULL,
    `request_uri` varchar(255) DEFAULT NULL,
    `http_method` varchar(16) DEFAULT NULL,
    `cost_ms` bigint DEFAULT NULL,
    `error_message` varchar(1000) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `created_by` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_by` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_sys_log_tenant_id` (`tenant_id`),
    KEY `idx_sys_log_trace_id` (`trace_id`),
    KEY `idx_sys_log_occurred_at` (`occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
