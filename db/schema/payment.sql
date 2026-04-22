CREATE TABLE IF NOT EXISTS `bacon_payment_order` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `payment_no` varchar(64) NOT NULL,
    `order_no` varchar(64) NOT NULL,
    `user_id` bigint NOT NULL,
    `channel_code` varchar(32) NOT NULL,
    `payment_status` varchar(16) NOT NULL,
    `amount` decimal(18,2) NOT NULL,
    `paid_amount` decimal(18,2) NOT NULL DEFAULT 0.00,
    `subject` varchar(255) NOT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_at` datetime(3) NOT NULL,
    `expired_at` datetime(3) NOT NULL,
    `paid_at` datetime(3) DEFAULT NULL,
    `closed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_tenant_user_created` (`tenant_id`, `user_id`, `created_at`),
    KEY `idx_tenant_status_expired` (`tenant_id`, `payment_status`, `expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_payment_callback_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `payment_no` varchar(64) NOT NULL,
    `order_no` varchar(64) NOT NULL,
    `channel_code` varchar(32) NOT NULL,
    `channel_transaction_no` varchar(128) DEFAULT NULL,
    `channel_status` varchar(64) NOT NULL,
    `raw_payload` json NOT NULL,
    `received_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_channel_txn` (`tenant_id`, `channel_code`, `channel_transaction_no`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_tenant_payment_received` (`tenant_id`, `payment_no`, `received_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bacon_payment_audit_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    `payment_no` varchar(64) NOT NULL,
    `action_type` varchar(64) NOT NULL,
    `before_status` varchar(16) DEFAULT NULL,
    `after_status` varchar(16) DEFAULT NULL,
    `operator_type` varchar(32) DEFAULT NULL,
    `operator_id` varchar(64) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_occurred` (`tenant_id`, `occurred_at`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_tenant_payment_action` (`tenant_id`, `payment_no`, `action_type`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
