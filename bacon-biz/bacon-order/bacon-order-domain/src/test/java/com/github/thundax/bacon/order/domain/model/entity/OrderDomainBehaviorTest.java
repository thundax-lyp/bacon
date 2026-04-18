package com.github.thundax.bacon.order.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderDomainBehaviorTest {

    @Test
    void createShouldInitializeStatusesAndAggregateAmounts() {
        OrderItem firstItem = OrderItem.create(
                1L, null, SkuId.of(101L), "sku-1", null, 2, Money.of(new BigDecimal("12.50"), CurrencyCode.RMB));
        OrderItem secondItem = OrderItem.create(
                2L, null, SkuId.of(102L), "sku-2", null, 1, Money.of(new BigDecimal("5.00"), CurrencyCode.RMB));

        Order order = Order.create(
                OrderId.of(1L), OrderNo.of("ORD-001"), UserId.of(2001L), null, List.of(firstItem, secondItem), "remark", null);

        assertEquals(OrderStatus.CREATED, order.getOrderStatus());
        assertEquals(PayStatus.UNPAID, order.getPayStatus());
        assertEquals(InventoryStatus.UNRESERVED, order.getInventoryStatus());
        assertEquals(CurrencyCode.RMB, order.getCurrencyCode());
        assertEquals(Money.of(new BigDecimal("30.00"), CurrencyCode.RMB), order.getTotalAmount());
        assertEquals(order.getTotalAmount(), order.getPayableAmount());
        assertNotNull(order.getCreatedAt());
        assertEquals(order.getCreatedAt().plusSeconds(1800), order.getExpiredAt());
    }

    @Test
    void shouldFollowReserveAndPaymentHappyPath() {
        Order order = newOrder();

        order.markReservingStock();
        boolean reserved = order.recordInventoryReservationResult(
                InventoryStatus.RESERVED, ReservationNo.of("RSV-001"), WarehouseCode.of("WH-001"), null, null);
        boolean paymentCreated =
                order.recordPaymentCreationResult(PaymentNo.of("PAY-001"), PayStatus.PAYING, "ALIPAY", null);
        order.markPaid(
                PaymentNo.of("PAY-001"), "ALIPAY", Money.of(new BigDecimal("20.00"), CurrencyCode.RMB), Instant.now());

        assertTrue(reserved);
        assertTrue(paymentCreated);
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertEquals(PayStatus.PAID, order.getPayStatus());
        assertEquals(InventoryStatus.RESERVED, order.getInventoryStatus());
        assertEquals(PaymentNo.of("PAY-001"), order.getPaymentNo());
        assertNotNull(order.getPaidAt());
    }

    @Test
    void shouldCloseOrderWhenReservationFails() {
        Order order = newOrder();
        order.markReservingStock();

        boolean reserved = order.recordInventoryReservationResult(
                InventoryStatus.FAILED, ReservationNo.of("RSV-002"), WarehouseCode.of("WH-001"), "no stock", "close");

        assertEquals(false, reserved);
        assertEquals(OrderStatus.CLOSED, order.getOrderStatus());
        assertEquals(InventoryStatus.FAILED, order.getInventoryStatus());
        assertEquals("close", order.getCloseReason());
        assertNotNull(order.getClosedAt());
    }

    @Test
    void shouldRejectPaymentBeforeReservationFlow() {
        Order order = newOrder();

        assertThrows(
                OrderDomainException.class,
                () -> order.markPaid(
                        PaymentNo.of("PAY-002"),
                        "ALIPAY",
                        Money.of(new BigDecimal("20.00"), CurrencyCode.RMB),
                        Instant.now()));
    }

    @Test
    void closeExpiredShouldClosePendingPaymentOrder() {
        Order order = newOrder();
        order.markReservingStock();
        order.recordInventoryReservationResult(
                InventoryStatus.RESERVED, ReservationNo.of("RSV-003"), WarehouseCode.of("WH-001"), null, null);
        order.recordPaymentCreationResult(PaymentNo.of("PAY-003"), PayStatus.PAYING, "ALIPAY", null);

        order.closeExpired("expired");

        assertEquals(OrderStatus.CLOSED, order.getOrderStatus());
        assertEquals(PayStatus.CLOSED, order.getPayStatus());
        assertEquals("expired", order.getCloseReason());
        assertNotNull(order.getClosedAt());
    }

    @Test
    void cancelShouldWorkDuringReservingStock() {
        Order order = newOrder();
        order.markReservingStock();

        order.cancel("user cancelled");

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        assertEquals(PayStatus.CLOSED, order.getPayStatus());
        assertEquals("user cancelled", order.getCancelReason());
        assertNotNull(order.getClosedAt());
    }

    @Test
    void recordInventoryReleaseResultShouldOnlyWriteInventoryFacts() {
        Order order = newOrder();
        order.markReservingStock();
        order.recordInventoryReservationResult(
                InventoryStatus.RESERVED, ReservationNo.of("RSV-004"), WarehouseCode.of("WH-001"), null, null);
        order.recordPaymentCreationResult(PaymentNo.of("PAY-004"), PayStatus.PAYING, "ALIPAY", null);
        order.cancel("cancelled");

        order.recordInventoryReleaseResult(
                InventoryStatus.RELEASED,
                ReservationNo.of("RSV-004"),
                WarehouseCode.of("WH-001"),
                "cancel release",
                Instant.parse("2026-04-18T10:00:00Z"),
                "ignored");

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        assertEquals(InventoryStatus.RELEASED, order.getInventoryStatus());
        assertEquals("cancel release", order.getInventoryReleaseReason());
        assertNotNull(order.getInventoryReleasedAt());
    }

    @Test
    void recordInventoryDeductionResultShouldWriteSuccessFacts() {
        Instant deductedAt = Instant.parse("2026-04-18T10:00:00Z");
        Order order = paidOrder();

        boolean deducted = order.recordInventoryDeductionResult(
                InventoryStatus.DEDUCTED,
                ReservationNo.of("RSV-005"),
                WarehouseCode.of("WH-001"),
                deductedAt,
                null);

        assertTrue(deducted);
        assertEquals(InventoryStatus.DEDUCTED, order.getInventoryStatus());
        assertEquals(deductedAt, order.getInventoryDeductedAt());
        assertEquals(WarehouseCode.of("WH-001"), order.getWarehouseCode());
    }

    @Test
    void recordInventoryDeductionResultShouldWriteFailureFacts() {
        Order order = paidOrder();

        boolean deducted = order.recordInventoryDeductionResult(
                InventoryStatus.FAILED,
                ReservationNo.of("RSV-006"),
                WarehouseCode.of("WH-001"),
                Instant.parse("2026-04-18T10:00:00Z"),
                "deduct failed");

        assertEquals(false, deducted);
        assertEquals(InventoryStatus.FAILED, order.getInventoryStatus());
        assertEquals("deduct failed", order.getInventoryFailureReason());
    }

    @Test
    void markPaymentFailedShouldCloseOrder() {
        Instant failedAt = Instant.parse("2026-04-18T10:00:00Z");
        Order order = pendingPaymentOrder();

        order.markPaymentFailed(PaymentNo.of("PAY-005"), "payment failed", "FAIL", failedAt);

        assertEquals(OrderStatus.CLOSED, order.getOrderStatus());
        assertEquals(PayStatus.FAILED, order.getPayStatus());
        assertEquals("payment failed", order.getPaymentFailureReason());
        assertEquals("FAIL", order.getPaymentChannelStatus());
        assertEquals(failedAt, order.getPaymentFailedAt());
        assertEquals("payment failed", order.getCloseReason());
        assertNotNull(order.getClosedAt());
    }

    @Test
    void reconstructShouldApplyTimeAndMoneyDefaults() {
        Order order = Order.reconstruct(
                OrderId.of(1L),
                OrderNo.of("ORD-RECON"),
                UserId.of(2001L),
                OrderStatus.CREATED,
                PayStatus.UNPAID,
                InventoryStatus.UNRESERVED,
                null,
                null,
                CurrencyCode.RMB,
                Money.of(new BigDecimal("18.00"), CurrencyCode.RMB),
                null,
                "remark",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertNotNull(order.getCreatedAt());
        assertEquals(order.getCreatedAt().plusSeconds(1800), order.getExpiredAt());
        assertEquals(Money.of(new BigDecimal("18.00"), CurrencyCode.RMB), order.getTotalAmount());
        assertEquals(null, order.getPayableAmount());
        assertEquals(null, order.getPaidAmount());
    }

    @Test
    void reconstructShouldPreserveTerminalFacts() {
        Instant createdAt = Instant.parse("2026-04-18T10:00:00Z");
        Instant expiredAt = createdAt.plusSeconds(1800);
        Instant paidAt = createdAt.plusSeconds(300);
        Instant inventoryReleasedAt = createdAt.plusSeconds(600);
        Instant inventoryDeductedAt = createdAt.plusSeconds(500);
        Instant paymentFailedAt = createdAt.plusSeconds(400);
        Instant closedAt = createdAt.plusSeconds(700);
        Order order = Order.reconstruct(
                OrderId.of(2L),
                OrderNo.of("ORD-TERMINAL"),
                UserId.of(2002L),
                OrderStatus.CLOSED,
                PayStatus.FAILED,
                InventoryStatus.RELEASED,
                PaymentNo.of("PAY-TERMINAL"),
                ReservationNo.of("RSV-TERMINAL"),
                CurrencyCode.RMB,
                Money.of(new BigDecimal("20.00"), CurrencyCode.RMB),
                Money.of(new BigDecimal("18.00"), CurrencyCode.RMB),
                "remark",
                "cancel",
                "closed",
                createdAt,
                expiredAt,
                paidAt,
                closedAt,
                com.github.thundax.bacon.order.domain.model.enums.PaymentChannel.ALIPAY,
                Money.of(new BigDecimal("18.00"), CurrencyCode.RMB),
                "FAIL",
                "payment failed",
                paymentFailedAt,
                WarehouseCode.of("WH-001"),
                "inventory failed",
                "released",
                inventoryReleasedAt,
                inventoryDeductedAt);

        assertEquals(createdAt, order.getCreatedAt());
        assertEquals(expiredAt, order.getExpiredAt());
        assertEquals(paidAt, order.getPaidAt());
        assertEquals(closedAt, order.getClosedAt());
        assertEquals(paymentFailedAt, order.getPaymentFailedAt());
        assertEquals(inventoryReleasedAt, order.getInventoryReleasedAt());
        assertEquals(inventoryDeductedAt, order.getInventoryDeductedAt());
        assertEquals(Money.of(new BigDecimal("18.00"), CurrencyCode.RMB), order.getPayableAmount());
        assertEquals(Money.of(new BigDecimal("18.00"), CurrencyCode.RMB), order.getPaidAmount());
    }

    @Test
    void markReservingStockShouldRejectTerminalOrder() {
        Order order = newOrder();
        order.cancel("cancelled");

        assertThrows(OrderDomainException.class, order::markReservingStock);
    }

    @Test
    void closeExpiredShouldRejectPaidOrder() {
        Order order = paidOrder();

        assertThrows(OrderDomainException.class, () -> order.closeExpired("expired"));
    }

    private static Order newOrder() {
        OrderItem item =
                OrderItem.create(1L, null, SkuId.of(101L), "sku-1", null, 2, Money.of(new BigDecimal("10.00")));
        return Order.create(
                OrderId.of(1L), OrderNo.of("ORD-100"), UserId.of(2001L), CurrencyCode.RMB, List.of(item), null, null);
    }

    private static Order pendingPaymentOrder() {
        Order order = newOrder();
        order.markReservingStock();
        order.recordInventoryReservationResult(
                InventoryStatus.RESERVED, ReservationNo.of("RSV-PAY"), WarehouseCode.of("WH-001"), null, null);
        order.recordPaymentCreationResult(PaymentNo.of("PAY-PENDING"), PayStatus.PAYING, "ALIPAY", null);
        return order;
    }

    private static Order paidOrder() {
        Order order = pendingPaymentOrder();
        order.markPaid(
                PaymentNo.of("PAY-SUCCESS"), "ALIPAY", Money.of(new BigDecimal("20.00"), CurrencyCode.RMB), Instant.now());
        return order;
    }
}
