CREATE TABLE IF NOT EXISTS `bacon_storage_object` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `tenant_id` VARCHAR(64) DEFAULT NULL COMMENT '所属租户业务键',
    `storage_type` VARCHAR(32) NOT NULL COMMENT '底层存储类型',
    `bucket_name` VARCHAR(128) DEFAULT NULL COMMENT '存储桶或本地逻辑目录',
    `object_key` VARCHAR(512) NOT NULL COMMENT '底层对象键，全局唯一',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `content_type` VARCHAR(128) NOT NULL COMMENT '内容类型',
    `size` BIGINT NOT NULL COMMENT '文件大小，字节',
    `access_url` VARCHAR(1024) NOT NULL COMMENT '当前访问地址',
    `object_status` VARCHAR(32) NOT NULL COMMENT '对象状态',
    `reference_status` VARCHAR(32) NOT NULL COMMENT '引用状态',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME(3) NOT NULL COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME(3) NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_object_key` (`object_key`),
    KEY `idx_tenant_status` (`tenant_id`, `object_status`, `reference_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一存储对象主数据表';

CREATE TABLE IF NOT EXISTS `bacon_storage_object_reference` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `object_id` BIGINT NOT NULL COMMENT '存储对象主键',
    `owner_type` VARCHAR(64) NOT NULL COMMENT '引用方类型',
    `owner_id` VARCHAR(64) NOT NULL COMMENT '引用方业务主键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_object_owner` (`object_id`, `owner_type`, `owner_id`),
    KEY `idx_owner` (`owner_type`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储对象引用关系表';

CREATE TABLE IF NOT EXISTS `bacon_storage_audit_log` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `tenant_id` VARCHAR(64) DEFAULT NULL COMMENT '所属租户业务键',
    `object_id` BIGINT DEFAULT NULL COMMENT '存储对象主键',
    `owner_type` VARCHAR(64) DEFAULT NULL COMMENT '引用方类型',
    `owner_id` VARCHAR(64) DEFAULT NULL COMMENT '引用方业务主键',
    `action_type` VARCHAR(64) NOT NULL COMMENT '审计动作类型',
    `before_status` VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
    `after_status` VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
    `operator_type` VARCHAR(32) DEFAULT NULL COMMENT '操作人类型',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人主键',
    `occurred_at` DATETIME(3) NOT NULL COMMENT '审计发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_occurred` (`tenant_id`, `occurred_at`),
    KEY `idx_object_occurred` (`object_id`, `occurred_at`),
    KEY `idx_operator_occurred` (`operator_id`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储对象审计日志表';
