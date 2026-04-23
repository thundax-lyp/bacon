package com.github.thundax.bacon.order.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import com.github.thundax.bacon.order.application.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.application.dto.OrderItemDTO;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.application.result.OrderPageResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderFacadeLocalContractTest {

    @Mock
    private OrderQueryApplicationService orderQueryApplicationService;

    @Mock
    private OrderPaymentResultApplicationService orderPaymentResultApplicationService;

    @Mock
    private OrderTimeoutApplicationService orderTimeoutApplicationService;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldMapOrderDetailFacadeToApplicationQuery() {
        when(orderQueryApplicationService.getByOrderNo(argThat(query ->
                        query != null && query.orderNo() != null && query.orderNo().value().equals("ORD-1"))))
                .thenReturn(detailDto());
        OrderReadFacadeLocalImpl facade = new OrderReadFacadeLocalImpl(orderQueryApplicationService);

        OrderDetailFacadeResponse response = facade.getByOrderNo(new OrderDetailFacadeRequest("ORD-1"));

        assertThat(response.getOrderNo()).isEqualTo("ORD-1");
        assertThat(response.getPayStatus()).isEqualTo("PAID");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getLineAmount()).isEqualByComparingTo("20");
        assertThat(response.getPaidAt()).isEqualTo(Instant.parse("2026-03-26T10:05:00Z"));
    }

    @Test
    void shouldMapOrderPageFacadeToApplicationQueryAndKeepPagination() {
        when(orderQueryApplicationService.page(argThat(query -> query != null
                        && query.getUserId().value().equals(2001L)
                        && query.getOrderNo().value().equals("ORD-1")
                        && query.getOrderStatus().name().equals("CREATED")
                        && query.getPayStatus().name().equals("UNPAID")
                        && query.getInventoryStatus().name().equals("UNRESERVED")
                        && query.getCreatedAtFrom().equals(Instant.parse("2026-03-26T10:00:00Z"))
                        && query.getCreatedAtTo().equals(Instant.parse("2026-03-27T10:00:00Z"))
                        && query.getPageNo() == 2
                        && query.getPageSize() == 20)))
                .thenReturn(new OrderPageResult(List.of(summaryDto()), 21, 2, 20));
        OrderReadFacadeLocalImpl facade = new OrderReadFacadeLocalImpl(orderQueryApplicationService);

        OrderPageFacadeResponse response = facade.page(new OrderPageFacadeRequest(
                2001L,
                "ORD-1",
                "CREATED",
                "UNPAID",
                "UNRESERVED",
                Instant.parse("2026-03-26T10:00:00Z"),
                Instant.parse("2026-03-27T10:00:00Z"),
                2,
                20));

        assertThat(response.getRecords()).hasSize(1);
        assertThat(response.getRecords().get(0).getOrderNo()).isEqualTo("ORD-1");
        assertThat(response.getTotal()).isEqualTo(21);
        assertThat(response.getPageNo()).isEqualTo(2);
        assertThat(response.getPageSize()).isEqualTo(20);
    }

    @Test
    void shouldMapPaymentResultFacadeToApplicationCommands() {
        OrderCommandFacadeLocalImpl facade =
                new OrderCommandFacadeLocalImpl(orderPaymentResultApplicationService, orderTimeoutApplicationService);

        facade.markPaid(new OrderMarkPaidFacadeRequest(
                "ORD-1", "PAY-1", "WECHAT", BigDecimal.TEN, Instant.parse("2026-03-26T10:05:00Z")));
        facade.markPaymentFailed(new OrderMarkPaymentFailedFacadeRequest(
                "ORD-2", "PAY-2", "insufficient balance", "FAILED", Instant.parse("2026-03-26T10:06:00Z")));

        verify(orderPaymentResultApplicationService)
                .markPaid(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.paymentNo().value().equals("PAY-1")
                        && command.channelCode().equals("WECHAT")
                        && command.paidAmount().compareTo(BigDecimal.TEN) == 0
                        && command.paidTime().equals(Instant.parse("2026-03-26T10:05:00Z"))));
        verify(orderPaymentResultApplicationService)
                .markPaymentFailed(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-2")
                        && command.paymentNo().value().equals("PAY-2")
                        && command.reason().equals("insufficient balance")
                        && command.channelStatus().equals("FAILED")
                        && command.failedTime().equals(Instant.parse("2026-03-26T10:06:00Z"))));
    }

    @Test
    void shouldMapCloseExpiredFacadeToApplicationCommand() {
        OrderCommandFacadeLocalImpl facade =
                new OrderCommandFacadeLocalImpl(orderPaymentResultApplicationService, orderTimeoutApplicationService);

        facade.closeExpiredOrder(new OrderCloseExpiredFacadeRequest("ORD-1", "expired"));

        verify(orderTimeoutApplicationService)
                .closeExpiredOrder(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.reason().equals("expired")));
    }

    private OrderDetailDTO detailDto() {
        return new OrderDetailDTO(
                1L,
                "ORD-1",
                2001L,
                "PAID",
                "PAID",
                "RESERVED",
                "PAY-1",
                "RSV-1",
                "CNY",
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(20),
                null,
                null,
                Instant.parse("2026-03-26T10:00:00Z"),
                Instant.parse("2026-03-26T10:30:00Z"),
                List.of(new OrderItemDTO(
                        3001L, "SKU-1", "https://cdn.example.com/sku.png", 2, BigDecimal.TEN, BigDecimal.valueOf(20))),
                "payment-ok",
                "inventory-ok",
                Instant.parse("2026-03-26T10:05:00Z"),
                null);
    }

    private OrderSummaryDTO summaryDto() {
        return new OrderSummaryDTO(
                1L,
                "ORD-1",
                2001L,
                "CREATED",
                "UNPAID",
                "UNRESERVED",
                null,
                null,
                "CNY",
                BigDecimal.TEN,
                BigDecimal.TEN,
                null,
                null,
                Instant.parse("2026-03-26T10:00:00Z"),
                Instant.parse("2026-03-26T10:30:00Z"));
    }
}
