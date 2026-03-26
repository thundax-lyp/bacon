CREATE TABLE IF NOT EXISTS `bacon_inventory_inventory` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `sku_id` bigint NOT NULL,
    `warehouse_id` bigint NOT NULL,
    `on_hand_quantity` int NOT NULL,
    `reserved_quantity` int NOT NULL,
    `available_quantity` int NOT NULL,
    `status` varchar(32) NOT NULL,
    `version` bigint NOT NULL DEFAULT 0,
    `created_by` bigint DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_by` bigint DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_sku` (`tenant_id`, `sku_id`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_inventory_reservation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `reservation_no` varchar(64) NOT NULL,
    `order_no` varchar(64) NOT NULL,
    `reservation_status` varchar(32) NOT NULL,
    `warehouse_id` bigint NOT NULL,
    `failure_reason` varchar(255) DEFAULT NULL,
    `release_reason` varchar(255) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `released_at` datetime(3) DEFAULT NULL,
    `deducted_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_order` (`tenant_id`, `order_no`),
    UNIQUE KEY `uk_tenant_reservation_no` (`tenant_id`, `reservation_no`),
    KEY `idx_tenant_status` (`tenant_id`, `reservation_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_inventory_reservation_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `reservation_no` varchar(64) NOT NULL,
    `sku_id` bigint NOT NULL,
    `quantity` int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_reservation_sku` (`tenant_id`, `reservation_no`, `sku_id`),
    KEY `idx_tenant_reservation_no` (`tenant_id`, `reservation_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_inventory_ledger` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_no` varchar(64) NOT NULL,
    `reservation_no` varchar(64) NOT NULL,
    `sku_id` bigint NOT NULL,
    `warehouse_id` bigint NOT NULL,
    `ledger_type` varchar(16) NOT NULL,
    `quantity` int NOT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_order_ledger` (`tenant_id`, `order_no`, `ledger_type`),
    KEY `idx_tenant_reservation` (`tenant_id`, `reservation_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_inventory_audit_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_no` varchar(64) DEFAULT NULL,
    `reservation_no` varchar(64) DEFAULT NULL,
    `action_type` varchar(64) NOT NULL,
    `operator_type` varchar(32) DEFAULT NULL,
    `operator_id` bigint DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_occurred` (`tenant_id`, `occurred_at`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_reservation_no` (`reservation_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_inventory_audit_outbox` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_no` varchar(64) DEFAULT NULL,
    `reservation_no` varchar(64) DEFAULT NULL,
    `action_type` varchar(64) NOT NULL,
    `operator_type` varchar(32) DEFAULT NULL,
    `operator_id` bigint DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `error_message` varchar(512) NOT NULL,
    `status` varchar(16) NOT NULL,
    `retry_count` int NOT NULL DEFAULT 0,
    `next_retry_at` datetime(3) DEFAULT NULL,
    `processing_owner` varchar(128) DEFAULT NULL,
    `lease_until` datetime(3) DEFAULT NULL,
    `claimed_at` datetime(3) DEFAULT NULL,
    `dead_reason` varchar(64) DEFAULT NULL,
    `failed_at` datetime(3) NOT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_status_failed` (`status`, `failed_at`),
    KEY `idx_tenant_order` (`tenant_id`, `order_no`),
    KEY `idx_status_next_retry` (`status`, `next_retry_at`),
    KEY `idx_status_next_retry_lease` (`status`, `next_retry_at`, `lease_until`),
    KEY `idx_processing_owner` (`processing_owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_inventory_audit_dead_letter` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `outbox_id` bigint NOT NULL,
    `tenant_id` bigint NOT NULL,
    `order_no` varchar(64) DEFAULT NULL,
    `reservation_no` varchar(64) DEFAULT NULL,
    `action_type` varchar(64) NOT NULL,
    `operator_type` varchar(32) DEFAULT NULL,
    `operator_id` bigint DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `retry_count` int NOT NULL,
    `error_message` varchar(512) NOT NULL,
    `dead_reason` varchar(64) NOT NULL,
    `dead_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_dead_at` (`dead_at`),
    KEY `idx_tenant_order` (`tenant_id`, `order_no`),
    KEY `idx_outbox_id` (`outbox_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
