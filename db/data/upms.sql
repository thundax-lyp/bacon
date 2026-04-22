-- Default administrator account:
--   tenant_id: 1000001
--   account: admin
--   password: Admin@123456

INSERT INTO `bacon_upms_tenant` (
    `tenant_id`, `code`, `name`, `status`
) VALUES (
    1000001, 'BACON', 'Bacon 默认租户', 'ACTIVE'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `status` = VALUES(`status`);

INSERT INTO `bacon_upms_department` (
    `id`, `tenant_id`, `code`, `name`, `parent_id`, `leader_user_id`,
    `sort`, `status`
) VALUES
    (
        1100001, 1000001, 'BACON_ROOT', 'Bacon 总部', NULL, 2000001,
        1, 'ENABLED'
    ),
    (
        1100002, 1000001, 'BACON_IT', '平台研发部', 1100001, 2000001,
        2, 'ENABLED'
    )
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `parent_id` = VALUES(`parent_id`),
    `leader_user_id` = VALUES(`leader_user_id`),
    `sort` = VALUES(`sort`),
    `status` = VALUES(`status`);

INSERT INTO `bacon_upms_post` (
    `id`, `tenant_id`, `code`, `name`, `department_id`, `status`
) VALUES (
    1200001, 1000001, 'PLATFORM_ADMIN', '平台管理员', 1100002, 'ENABLED'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `department_id` = VALUES(`department_id`),
    `status` = VALUES(`status`);

INSERT INTO `bacon_upms_user` (
    `id`, `tenant_id`, `name`, `avatar_object_id`, `department_id`,
    `status`, `deleted`
) VALUES (
    2000001, 1000001, '系统管理员', NULL, 1100002,
    'ENABLED', 0
) ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `name` = VALUES(`name`),
    `avatar_object_id` = VALUES(`avatar_object_id`),
    `department_id` = VALUES(`department_id`),
    `status` = VALUES(`status`),
    `deleted` = VALUES(`deleted`);

INSERT INTO `bacon_upms_user_identity` (
    `id`, `tenant_id`, `user_id`, `identity_type`, `identity_value`, `status`
) VALUES
    (
        2100001, 1000001, 2000001, 'ACCOUNT', 'admin', 'ACTIVE'
    ),
    (
        2100002, 1000001, 2000001, 'PHONE', '13800000000', 'ACTIVE'
    )
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `user_id` = VALUES(`user_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `bacon_upms_user_credential` (
    `id`, `tenant_id`, `user_id`, `identity_id`, `credential_type`, `factor_level`,
    `credential_value`, `status`, `need_change_password`, `failed_count`, `failed_limit`,
    `lock_reason`, `locked_until`, `expires_at`, `last_verified_at`
) VALUES (
    2200001, 1000001, 2000001, 2100001, 'PASSWORD', 'PRIMARY',
    '$2y$10$yjKSvevJS2WNdyBKKD1EBut7GNXMGCNNJfWpMtS5DILA9.sdEeASG', 'ACTIVE', 0, 0, 5,
    NULL, NULL, '2026-06-19 09:04:00.000', NULL
) ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `user_id` = VALUES(`user_id`),
    `identity_id` = VALUES(`identity_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`),
    `lock_reason` = VALUES(`lock_reason`),
    `locked_until` = VALUES(`locked_until`),
    `expires_at` = VALUES(`expires_at`),
    `last_verified_at` = VALUES(`last_verified_at`);

INSERT INTO `bacon_upms_role` (
    `id`, `tenant_id`, `code`, `name`, `role_type`, `data_scope_type`,
    `status`
) VALUES (
    1300001, 1000001, 'SUPER_ADMIN', '超级管理员', 'SYSTEM_ROLE', 'ALL',
    'ENABLED'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `role_type` = VALUES(`role_type`),
    `data_scope_type` = VALUES(`data_scope_type`),
    `status` = VALUES(`status`);

INSERT INTO `bacon_upms_menu` (
    `id`, `tenant_id`, `menu_type`, `name`, `parent_id`,
    `route_path`, `component_name`, `icon`, `sort`, `permission_code`
) VALUES
    (
        1400001, 1000001, 'DIRECTORY', '系统管理', NULL,
        '/system', 'Layout', 'setting', 10,
        'upms:system:view'
    ),
    (
        1400002, 1000001, 'MENU', '用户管理', 1400001,
        '/system/users', 'system/user/index', 'user', 11,
        'upms:user:view'
    ),
    (
        1400003, 1000001, 'MENU', '角色管理', 1400001,
        '/system/roles', 'system/role/index', 'safety-certificate', 12,
        'upms:role:view'
    ),
    (
        1400004, 1000001, 'MENU', '租户管理', 1400001,
        '/system/tenants', 'system/tenant/index', 'office-building', 13,
        'upms:tenant:view'
    ),
    (
        1400005, 1000001, 'BUTTON', '用户新增', 1400002,
        NULL, NULL, NULL, 1,
        'upms:user:create'
    ),
    (
        1400006, 1000001, 'BUTTON', '用户停用', 1400002,
        NULL, NULL, NULL, 2,
        'upms:user:disable'
    )
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `menu_type` = VALUES(`menu_type`),
    `name` = VALUES(`name`),
    `parent_id` = VALUES(`parent_id`),
    `route_path` = VALUES(`route_path`),
    `component_name` = VALUES(`component_name`),
    `icon` = VALUES(`icon`),
    `sort` = VALUES(`sort`),
    `permission_code` = VALUES(`permission_code`);

INSERT INTO `bacon_upms_resource` (
    `id`, `tenant_id`, `code`, `name`, `resource_type`,
    `path`, `method`, `status`
) VALUES
    (
        1500001, 1000001, 'UPMS_USER_LIST', '查询用户列表', 'API',
        '/upms/users', 'GET', 'ENABLED'
    ),
    (
        1500002, 1000001, 'UPMS_USER_CREATE', '创建用户', 'API',
        '/upms/users', 'POST', 'ENABLED'
    ),
    (
        1500003, 1000001, 'UPMS_ROLE_LIST', '查询角色列表', 'API',
        '/upms/roles', 'GET', 'ENABLED'
    ),
    (
        1500004, 1000001, 'AUTH_SESSION_INVALIDATE', '失效会话', 'API',
        '/auth/sessions/invalidate', 'POST', 'ENABLED'
    )
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `name` = VALUES(`name`),
    `resource_type` = VALUES(`resource_type`),
    `path` = VALUES(`path`),
    `method` = VALUES(`method`),
    `status` = VALUES(`status`);

INSERT INTO `bacon_upms_user_role_rel` (`id`, `tenant_id`, `user_id`, `role_id`) VALUES
    (1600001, 1000001, 2000001, 1300001)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `bacon_upms_user_post_rel` (`id`, `tenant_id`, `user_id`, `post_id`) VALUES
    (1600101, 1000001, 2000001, 1200001)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `bacon_upms_role_menu_rel` (`id`, `tenant_id`, `role_id`, `menu_id`) VALUES
    (1700001, 1000001, 1300001, 1400001),
    (1700002, 1000001, 1300001, 1400002),
    (1700003, 1000001, 1300001, 1400003),
    (1700004, 1000001, 1300001, 1400004),
    (1700005, 1000001, 1300001, 1400005),
    (1700006, 1000001, 1300001, 1400006)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `bacon_upms_role_resource_rel` (`id`, `tenant_id`, `role_id`, `resource_id`) VALUES
    (1800001, 1000001, 1300001, 1500001),
    (1800002, 1000001, 1300001, 1500002),
    (1800003, 1000001, 1300001, 1500003),
    (1800004, 1000001, 1300001, 1500004)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `bacon_upms_data_permission_rule` (
    `id`, `tenant_id`, `role_id`, `data_scope_type`
) VALUES (
    1900001, 1000001, 1300001, 'ALL'
) ON DUPLICATE KEY UPDATE
    `data_scope_type` = VALUES(`data_scope_type`);

-- Current SUPER_ADMIN seed uses data_scope_type = ALL,
-- so bacon_upms_role_data_scope_rel intentionally remains empty.

INSERT INTO `bacon_upms_audit_log` (
    `id`, `tenant_id`, `operator_id`, `object_type`, `object_id`,
    `action_type`, `before_summary`, `after_summary`,
    `request_source`, `result_status`, `occurred_at`
) VALUES (
    1950001, 'T1000001', '2000001', 'USER', '2000001',
    'CREATE', NULL,
    JSON_OBJECT(
        'id', '2000001',
        'account', 'admin',
        'name', '系统管理员',
        'phoneMasked', '138****0000',
        'departmentId', 'D1100002',
        'status', 'ENABLED',
        'deleted', 0
    ),
    'SYSTEM_JOB', 'SUCCESS', '2026-03-21 09:09:00.000'
) ON DUPLICATE KEY UPDATE
    `after_summary` = VALUES(`after_summary`),
    `request_source` = VALUES(`request_source`),
    `result_status` = VALUES(`result_status`),
    `occurred_at` = VALUES(`occurred_at`);

INSERT INTO `bacon_upms_sys_log` (
    `id`, `tenant_id`, `trace_id`, `request_id`, `module`, `action`,
    `event_type`, `result`, `operator_id`, `operator_name`,
    `client_ip`, `request_uri`, `http_method`, `cost_ms`, `error_message`,
    `occurred_at`
) VALUES (
    1960001, 1000001, 'trace-upms-seed-0001', 'req-upms-seed-0001', 'upms', 'Seed default admin data',
    'CREATE', 'SUCCESS', '2000001', '系统管理员',
    '127.0.0.1', '/internal/db/seed/upms', 'POST', 15, NULL,
    '2026-03-21 09:09:30.000'
) ON DUPLICATE KEY UPDATE
    `module` = VALUES(`module`),
    `action` = VALUES(`action`),
    `event_type` = VALUES(`event_type`),
    `result` = VALUES(`result`),
    `operator_id` = VALUES(`operator_id`),
    `operator_name` = VALUES(`operator_name`),
    `client_ip` = VALUES(`client_ip`),
    `request_uri` = VALUES(`request_uri`),
    `http_method` = VALUES(`http_method`),
    `cost_ms` = VALUES(`cost_ms`),
    `error_message` = VALUES(`error_message`),
    `occurred_at` = VALUES(`occurred_at`);
