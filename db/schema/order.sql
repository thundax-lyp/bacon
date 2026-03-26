CREATE TABLE IF NOT EXISTS `bacon_order_order` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_no` varchar(64) NOT NULL,
    `user_id` bigint NOT NULL,
    `order_status` varchar(32) NOT NULL,
    `pay_status` varchar(16) NOT NULL,
    `inventory_status` varchar(16) NOT NULL,
    `currency_code` varchar(16) NOT NULL,
    `total_amount` decimal(18,2) NOT NULL,
    `payable_amount` decimal(18,2) NOT NULL,
    `remark` varchar(255) DEFAULT NULL,
    `cancel_reason` varchar(32) DEFAULT NULL,
    `close_reason` varchar(64) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_at` datetime(3) NOT NULL,
    `expired_at` datetime(3) NOT NULL,
    `paid_at` datetime(3) DEFAULT NULL,
    `closed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_tenant_user_created` (`tenant_id`, `user_id`, `created_at`),
    KEY `idx_tenant_order_status_created` (`tenant_id`, `order_status`, `created_at`),
    KEY `idx_tenant_expired_status` (`tenant_id`, `expired_at`, `order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `bacon_order_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `sku_id` bigint NOT NULL,
    `sku_name` varchar(128) NOT NULL,
    `quantity` int NOT NULL,
    `sale_price` decimal(18,2) NOT NULL,
    `line_amount` decimal(18,2) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_order` (`tenant_id`, `order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `bacon_order_payment_snapshot` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `payment_no` varchar(64) NOT NULL,
    `channel_code` varchar(32) NOT NULL,
    `pay_status` varchar(16) NOT NULL,
    `paid_amount` decimal(18,2) DEFAULT NULL,
    `paid_time` datetime(3) DEFAULT NULL,
    `failure_reason` varchar(255) DEFAULT NULL,
    `channel_status` varchar(64) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `bacon_order_inventory_snapshot` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `reservation_no` varchar(64) NOT NULL,
    `inventory_status` varchar(16) NOT NULL,
    `warehouse_id` bigint DEFAULT NULL,
    `failure_reason` varchar(255) DEFAULT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    UNIQUE KEY `uk_reservation_no` (`reservation_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `bacon_order_audit_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `order_no` varchar(64) NOT NULL,
    `action_type` varchar(64) NOT NULL,
    `before_status` varchar(32) DEFAULT NULL,
    `after_status` varchar(32) DEFAULT NULL,
    `operator_type` varchar(32) DEFAULT NULL,
    `operator_id` bigint DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_occurred` (`tenant_id`, `occurred_at`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
