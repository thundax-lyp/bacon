-- Inventory seed data for local verification.
-- Suggested verification path:
-- 1) query stock by tenantId/skuId
-- 2) query reservation/ledger/audit by orderNo
-- 3) query outbox/dead-letter for compensation visibility

INSERT INTO `bacon_inventory_inventory` (
    `id`, `tenant_id`, `sku_id`, `warehouse_code`,
    `on_hand_quantity`, `reserved_quantity`,
    `status`, `version`, `created_by`, `created_at`, `updated_by`, `updated_at`
) VALUES
    (5000001, 1001, 3001001, 'WH-001', 200, 30, 'ACTIVE', 1, 2000001, '2026-03-26 10:00:00.000', 2000001, '2026-03-26 10:00:00.000'),
    (5000002, 1001, 3001002, 'WH-001', 120, 0, 'ACTIVE', 1, 2000001, '2026-03-26 10:00:30.000', 2000001, '2026-03-26 10:00:30.000')
ON DUPLICATE KEY UPDATE
    `warehouse_code` = VALUES(`warehouse_code`),
    `on_hand_quantity` = VALUES(`on_hand_quantity`),
    `reserved_quantity` = VALUES(`reserved_quantity`),
    `status` = VALUES(`status`),
    `version` = VALUES(`version`),
    `updated_by` = VALUES(`updated_by`),
    `updated_at` = VALUES(`updated_at`);

INSERT INTO `bacon_inventory_reservation` (
    `id`, `tenant_id`, `reservation_no`, `order_no`, `reservation_status`, `warehouse_code`,
    `failure_reason`, `release_reason`, `created_at`, `released_at`, `deducted_at`
) VALUES
    (5100001, 1001, 'RSV-20260326-0001', 'ORDER-INV-20260326-0001', 'RESERVED', 'WH-001',
        NULL, NULL, '2026-03-26 10:05:00.000', NULL, NULL),
    (5100002, 1001, 'RSV-20260326-0002', 'ORDER-INV-20260326-0002', 'FAILED', 'WH-001',
        'INSUFFICIENT_STOCK', NULL, '2026-03-26 10:06:00.000', NULL, NULL)
ON DUPLICATE KEY UPDATE
    `reservation_no` = VALUES(`reservation_no`),
    `reservation_status` = VALUES(`reservation_status`),
    `warehouse_code` = VALUES(`warehouse_code`),
    `failure_reason` = VALUES(`failure_reason`),
    `release_reason` = VALUES(`release_reason`),
    `released_at` = VALUES(`released_at`),
    `deducted_at` = VALUES(`deducted_at`);

INSERT INTO `bacon_inventory_reservation_item` (
    `id`, `tenant_id`, `reservation_no`, `sku_id`, `quantity`
) VALUES
    (5200001, 1001, 'RSV-20260326-0001', 3001001, 20),
    (5200002, 1001, 'RSV-20260326-0002', 3001001, 500)
ON DUPLICATE KEY UPDATE
    `quantity` = VALUES(`quantity`);

INSERT INTO `bacon_inventory_ledger` (
    `id`, `tenant_id`, `order_no`, `reservation_no`, `sku_id`, `warehouse_code`,
    `ledger_type`, `quantity`, `occurred_at`
) VALUES
    (5300001, 1001, 'ORDER-INV-20260326-0001', 'RSV-20260326-0001', 3001001, 'WH-001', 'RESERVE', 20, '2026-03-26 10:05:10.000'),
    (5300002, 1001, 'ORDER-INV-20260326-0001', 'RSV-20260326-0001', 3001001, 'WH-001', 'RELEASE', 5, '2026-03-26 10:05:40.000')
ON DUPLICATE KEY UPDATE
    `ledger_type` = VALUES(`ledger_type`),
    `quantity` = VALUES(`quantity`),
    `occurred_at` = VALUES(`occurred_at`);

INSERT INTO `bacon_inventory_audit_log` (
    `id`, `tenant_id`, `order_no`, `reservation_no`,
    `action_type`, `operator_type`, `operator_id`, `occurred_at`
) VALUES
    (5400001, 1001, 'ORDER-INV-20260326-0001', 'RSV-20260326-0001', 'RESERVE', 'SYSTEM', 0, '2026-03-26 10:05:12.000')
ON DUPLICATE KEY UPDATE
    `action_type` = VALUES(`action_type`),
    `occurred_at` = VALUES(`occurred_at`);

INSERT INTO `bacon_inventory_audit_outbox` (
    `id`, `event_code`, `tenant_id`, `order_no`, `reservation_no`,
    `action_type`, `operator_type`, `operator_id`, `occurred_at`,
    `error_message`, `status`, `retry_count`, `next_retry_at`,
    `dead_reason`, `failed_at`, `updated_at`
) VALUES
    (5500001, 'IAO-20260326-0001', 1001, 'ORDER-INV-20260326-0003', 'RSV-20260326-0003',
        'RESERVE_FAILED', 'SYSTEM', 0, '2026-03-26 10:07:00.000',
        'DB_TIMEOUT', 'RETRYING', 2, '2026-03-26 10:12:00.000',
        NULL, '2026-03-26 10:07:05.000', '2026-03-26 10:09:00.000'),
    (5500002, 'IAO-20260326-0002', 1001, 'ORDER-INV-20260326-0004', 'RSV-20260326-0004',
        'DEDUCT', 'SYSTEM', 0, '2026-03-26 10:08:00.000',
        'WRITE_CONFLICT', 'DEAD', 7, NULL,
        'MAX_RETRIES_EXCEEDED', '2026-03-26 10:08:05.000', '2026-03-26 10:20:00.000')
ON DUPLICATE KEY UPDATE
    `error_message` = VALUES(`error_message`),
    `status` = VALUES(`status`),
    `retry_count` = VALUES(`retry_count`),
    `next_retry_at` = VALUES(`next_retry_at`),
    `dead_reason` = VALUES(`dead_reason`),
    `updated_at` = VALUES(`updated_at`);

INSERT INTO `bacon_inventory_audit_dead_letter` (
    `id`, `outbox_id`, `event_code`, `tenant_id`, `order_no`, `reservation_no`,
    `action_type`, `operator_type`, `operator_id`, `occurred_at`,
    `retry_count`, `error_message`, `dead_reason`, `dead_at`
) VALUES
    (5600001, 5500002, 'IAO-20260326-0002', 1001, 'ORDER-INV-20260326-0004', 'RSV-20260326-0004',
        'DEDUCT', 'SYSTEM', 0, '2026-03-26 10:08:00.000',
        7, 'WRITE_CONFLICT', 'MAX_RETRIES_EXCEEDED', '2026-03-26 10:20:00.000')
ON DUPLICATE KEY UPDATE
    `retry_count` = VALUES(`retry_count`),
    `error_message` = VALUES(`error_message`),
    `dead_reason` = VALUES(`dead_reason`),
    `dead_at` = VALUES(`dead_at`);

INSERT INTO `bacon_inventory_audit_replay_task` (
    `id`, `tenant_id`, `task_no`, `status`, `total_count`,
    `processed_count`, `success_count`, `failed_count`, `replay_key_prefix`,
    `operator_type`, `operator_id`, `processing_owner`, `lease_until`, `last_error`,
    `created_at`, `started_at`, `paused_at`, `finished_at`, `updated_at`
) VALUES
    (5700001, 1001, 'RPT-20260326-0001', 'RUNNING', 1,
        0, 0, 0, 'replay-20260326',
        'SYSTEM', 0, 'inventory-worker-1', '2026-03-26 10:31:00.000', NULL,
        '2026-03-26 10:30:00.000', '2026-03-26 10:30:05.000', NULL, NULL, '2026-03-26 10:30:10.000')
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `processed_count` = VALUES(`processed_count`),
    `success_count` = VALUES(`success_count`),
    `failed_count` = VALUES(`failed_count`),
    `replay_key_prefix` = VALUES(`replay_key_prefix`),
    `processing_owner` = VALUES(`processing_owner`),
    `lease_until` = VALUES(`lease_until`),
    `last_error` = VALUES(`last_error`),
    `started_at` = VALUES(`started_at`),
    `paused_at` = VALUES(`paused_at`),
    `finished_at` = VALUES(`finished_at`),
    `updated_at` = VALUES(`updated_at`);

INSERT INTO `bacon_inventory_audit_replay_task_item` (
    `id`, `task_id`, `tenant_id`, `dead_letter_id`, `item_status`,
    `replay_status`, `replay_key`, `result_message`,
    `started_at`, `finished_at`, `updated_at`
) VALUES
    (5800001, 5700001, 1001, 5600001, 'PROCESSING',
        'PENDING', 'replay-20260326-5600001', '等待重放执行',
        '2026-03-26 10:30:06.000', NULL, '2026-03-26 10:30:10.000')
ON DUPLICATE KEY UPDATE
    `item_status` = VALUES(`item_status`),
    `replay_status` = VALUES(`replay_status`),
    `replay_key` = VALUES(`replay_key`),
    `result_message` = VALUES(`result_message`),
    `started_at` = VALUES(`started_at`),
    `finished_at` = VALUES(`finished_at`),
    `updated_at` = VALUES(`updated_at`);
