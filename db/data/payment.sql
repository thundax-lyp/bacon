-- Payment seed data for local verification.
-- Suggested verification path:
-- 1) query payment detail by paymentNo/orderNo
-- 2) inspect callback history for success and failure cases
-- 3) inspect audit logs for create/callback/close transitions

INSERT INTO `bacon_payment_order` (
    `id`, `tenant_id`, `payment_no`, `order_no`, `user_id`,
    `channel_code`, `payment_status`, `amount`, `paid_amount`, `subject`,
    `created_at`, `updated_at`, `expired_at`, `paid_at`, `closed_at`
) VALUES
    (7000001, 1001, 'PAY-20260327-0001', 'ORDER-PAY-20260327-0001', 2000001,
        'MOCK', 'PAID', 88.80, 88.80, '订单支付-成功样例',
        '2026-03-27 10:00:00.000', '2026-03-27 10:01:15.000', '2026-03-27 10:30:00.000',
        '2026-03-27 10:01:12.000', NULL),
    (7000002, 1001, 'PAY-20260327-0002', 'ORDER-PAY-20260327-0002', 2000002,
        'MOCK', 'FAILED', 128.00, 0.00, '订单支付-失败样例',
        '2026-03-27 10:05:00.000', '2026-03-27 10:06:20.000', '2026-03-27 10:35:00.000',
        NULL, NULL),
    (7000003, 1001, 'PAY-20260327-0003', 'ORDER-PAY-20260327-0003', 2000003,
        'MOCK', 'CLOSED', 66.60, 0.00, '订单支付-关闭样例',
        '2026-03-27 10:10:00.000', '2026-03-27 10:15:00.000', '2026-03-27 10:40:00.000',
        NULL, '2026-03-27 10:15:00.000')
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `channel_code` = VALUES(`channel_code`),
    `payment_status` = VALUES(`payment_status`),
    `amount` = VALUES(`amount`),
    `paid_amount` = VALUES(`paid_amount`),
    `subject` = VALUES(`subject`),
    `updated_at` = VALUES(`updated_at`),
    `expired_at` = VALUES(`expired_at`),
    `paid_at` = VALUES(`paid_at`),
    `closed_at` = VALUES(`closed_at`);

INSERT INTO `bacon_payment_callback_record` (
    `id`, `tenant_id`, `payment_no`, `order_no`, `channel_code`,
    `channel_transaction_no`, `channel_status`, `raw_payload`, `received_at`
) VALUES
    (7100001, 1001, 'PAY-20260327-0001', 'ORDER-PAY-20260327-0001', 'MOCK',
        'MOCK-TXN-20260327-0001', 'SUCCESS',
        JSON_OBJECT('paymentNo', 'PAY-20260327-0001', 'tradeStatus', 'SUCCESS', 'amount', 88.80),
        '2026-03-27 10:01:10.000'),
    (7100002, 1001, 'PAY-20260327-0002', 'ORDER-PAY-20260327-0002', 'MOCK',
        NULL, 'FAILED',
        JSON_OBJECT('paymentNo', 'PAY-20260327-0002', 'tradeStatus', 'FAILED', 'reason', 'USER_ABORTED'),
        '2026-03-27 10:06:18.000')
ON DUPLICATE KEY UPDATE
    `order_no` = VALUES(`order_no`),
    `channel_status` = VALUES(`channel_status`),
    `raw_payload` = VALUES(`raw_payload`),
    `received_at` = VALUES(`received_at`);

INSERT INTO `bacon_payment_audit_log` (
    `id`, `tenant_id`, `payment_no`, `action_type`,
    `before_status`, `after_status`, `operator_type`, `operator_id`, `occurred_at`
) VALUES
    (7200001, 1001, 'PAY-20260327-0001', 'CREATE', NULL, 'PAYING', 'SYSTEM', 0, '2026-03-27 10:00:00.000'),
    (7200002, 1001, 'PAY-20260327-0001', 'CALLBACK_PAID', 'PAYING', 'PAID', 'CHANNEL', 0, '2026-03-27 10:01:12.000'),
    (7200003, 1001, 'PAY-20260327-0002', 'CREATE', NULL, 'PAYING', 'SYSTEM', 0, '2026-03-27 10:05:00.000'),
    (7200004, 1001, 'PAY-20260327-0002', 'CALLBACK_FAILED', 'PAYING', 'FAILED', 'CHANNEL', 0, '2026-03-27 10:06:20.000'),
    (7200005, 1001, 'PAY-20260327-0003', 'CREATE', NULL, 'PAYING', 'SYSTEM', 0, '2026-03-27 10:10:00.000'),
    (7200006, 1001, 'PAY-20260327-0003', 'CLOSE', 'PAYING', 'CLOSED', 'SYSTEM', 0, '2026-03-27 10:15:00.000')
ON DUPLICATE KEY UPDATE
    `action_type` = VALUES(`action_type`),
    `before_status` = VALUES(`before_status`),
    `after_status` = VALUES(`after_status`),
    `operator_type` = VALUES(`operator_type`),
    `operator_id` = VALUES(`operator_id`),
    `occurred_at` = VALUES(`occurred_at`);
