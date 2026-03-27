CREATE TABLE IF NOT EXISTS `bacon_storage_object` (
    `id` BIGINT NOT NULL,
    `tenant_id` VARCHAR(64) DEFAULT NULL,
    `storage_type` VARCHAR(32) NOT NULL,
    `bucket_name` VARCHAR(128) DEFAULT NULL,
    `object_key` VARCHAR(512) NOT NULL,
    `original_filename` VARCHAR(255) NOT NULL,
    `content_type` VARCHAR(128) NOT NULL,
    `size` BIGINT NOT NULL,
    `access_url` VARCHAR(1024) NOT NULL,
    `object_status` VARCHAR(32) NOT NULL,
    `reference_status` VARCHAR(32) NOT NULL,
    `created_by` BIGINT DEFAULT NULL,
    `created_at` DATETIME(3) NOT NULL,
    `updated_by` BIGINT DEFAULT NULL,
    `updated_at` DATETIME(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_object_key` (`object_key`),
    KEY `idx_tenant_status` (`tenant_id`, `object_status`, `reference_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_storage_object_reference` (
    `id` BIGINT NOT NULL,
    `object_id` BIGINT NOT NULL,
    `owner_type` VARCHAR(64) NOT NULL,
    `owner_id` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_object_owner` (`object_id`, `owner_type`, `owner_id`),
    KEY `idx_owner` (`owner_type`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_storage_audit_log` (
    `id` BIGINT NOT NULL,
    `tenant_id` VARCHAR(64) DEFAULT NULL,
    `object_id` BIGINT DEFAULT NULL,
    `owner_type` VARCHAR(64) DEFAULT NULL,
    `owner_id` VARCHAR(64) DEFAULT NULL,
    `action_type` VARCHAR(64) NOT NULL,
    `before_status` VARCHAR(32) DEFAULT NULL,
    `after_status` VARCHAR(32) DEFAULT NULL,
    `operator_type` VARCHAR(32) DEFAULT NULL,
    `operator_id` BIGINT DEFAULT NULL,
    `occurred_at` DATETIME(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_occurred` (`tenant_id`, `occurred_at`),
    KEY `idx_object_occurred` (`object_id`, `occurred_at`),
    KEY `idx_operator_occurred` (`operator_id`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
