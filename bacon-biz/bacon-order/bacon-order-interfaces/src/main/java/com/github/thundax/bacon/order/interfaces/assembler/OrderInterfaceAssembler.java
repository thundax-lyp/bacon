package com.github.thundax.bacon.order.interfaces.assembler;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderItemFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderSummaryFacadeResponse;
import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.application.command.CreateOrderItemCommand;
import com.github.thundax.bacon.order.application.command.OrderCancelCommand;
import com.github.thundax.bacon.order.application.command.OrderCloseExpiredCommand;
import com.github.thundax.bacon.order.application.command.OrderMarkPaidCommand;
import com.github.thundax.bacon.order.application.command.OrderMarkPaymentFailedCommand;
import com.github.thundax.bacon.order.application.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.application.dto.OrderItemDTO;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.query.OrderByOrderNoQuery;
import com.github.thundax.bacon.order.application.query.OrderPageQuery;
import com.github.thundax.bacon.order.application.result.OrderOutboxDeadLetterReplayResult;
import com.github.thundax.bacon.order.application.result.OrderPageResult;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.interfaces.request.CancelOrderRequest;
import com.github.thundax.bacon.order.interfaces.request.CreateOrderItemRequest;
import com.github.thundax.bacon.order.interfaces.request.CreateOrderRequest;
import com.github.thundax.bacon.order.interfaces.request.OrderCloseExpiredRequest;
import com.github.thundax.bacon.order.interfaces.request.OrderMarkPaidRequest;
import com.github.thundax.bacon.order.interfaces.request.OrderMarkPaymentFailedRequest;
import com.github.thundax.bacon.order.interfaces.request.OrderPageRequest;
import com.github.thundax.bacon.order.interfaces.response.OrderDetailResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderItemResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderOutboxDeadLetterReplayResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderPageResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderSummaryResponse;
import java.util.List;

public final class OrderInterfaceAssembler {

    private OrderInterfaceAssembler() {}

    public static CreateOrderCommand toCreateCommand(CreateOrderRequest request) {
        if (request == null) {
            return null;
        }
        List<CreateOrderItemCommand> itemCommands = request.items() == null
                ? List.of()
                : request.items().stream().map(CreateOrderItemRequest::toCommand).toList();
        return new CreateOrderCommand(
                UserIdCodec.toDomain(request.userId()),
                request.currencyCode(),
                request.channelCode(),
                request.remark(),
                request.expiredAt(),
                itemCommands);
    }

    public static OrderCancelCommand toCancelCommand(String orderNo, CancelOrderRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderCancelCommand(OrderNoCodec.toDomain(orderNo), request.reason());
    }

    public static OrderByOrderNoQuery toByOrderNoQuery(String orderNo) {
        return new OrderByOrderNoQuery(OrderNoCodec.toDomain(orderNo));
    }

    public static OrderPageQuery toPageQuery(OrderPageFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderPageQuery(
                UserIdCodec.toDomain(request.getUserId()),
                OrderNoCodec.toDomain(request.getOrderNo()),
                request.getOrderStatus() == null ? null : OrderStatus.from(request.getOrderStatus()),
                request.getPayStatus() == null ? null : PayStatus.from(request.getPayStatus()),
                request.getInventoryStatus() == null ? null : InventoryStatus.from(request.getInventoryStatus()),
                request.getCreatedAtFrom(),
                request.getCreatedAtTo(),
                request.getPageNo(),
                request.getPageSize());
    }

    public static OrderPageQuery toPageQuery(OrderPageRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderPageQuery(
                UserIdCodec.toDomain(request.getUserId()),
                OrderNoCodec.toDomain(request.getOrderNo()),
                request.getOrderStatus() == null ? null : OrderStatus.from(request.getOrderStatus()),
                request.getPayStatus() == null ? null : PayStatus.from(request.getPayStatus()),
                request.getInventoryStatus() == null ? null : InventoryStatus.from(request.getInventoryStatus()),
                request.getCreatedAtFrom(),
                request.getCreatedAtTo(),
                request.getPageNo(),
                request.getPageSize());
    }

    public static OrderMarkPaidCommand toMarkPaidCommand(OrderMarkPaidFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderMarkPaidCommand(
                OrderNoCodec.toDomain(request.getOrderNo()),
                PaymentNoCodec.toDomain(request.getPaymentNo()),
                request.getChannelCode(),
                request.getPaidAmount(),
                request.getPaidTime());
    }

    public static OrderMarkPaidCommand toMarkPaidCommand(OrderMarkPaidRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderMarkPaidCommand(
                OrderNoCodec.toDomain(request.orderNo()),
                PaymentNoCodec.toDomain(request.paymentNo()),
                request.channelCode(),
                request.paidAmount(),
                request.paidTime());
    }

    public static OrderMarkPaymentFailedCommand toMarkPaymentFailedCommand(OrderMarkPaymentFailedFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderMarkPaymentFailedCommand(
                OrderNoCodec.toDomain(request.getOrderNo()),
                PaymentNoCodec.toDomain(request.getPaymentNo()),
                request.getReason(),
                request.getChannelStatus(),
                request.getFailedTime());
    }

    public static OrderMarkPaymentFailedCommand toMarkPaymentFailedCommand(OrderMarkPaymentFailedRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderMarkPaymentFailedCommand(
                OrderNoCodec.toDomain(request.orderNo()),
                PaymentNoCodec.toDomain(request.paymentNo()),
                request.reason(),
                request.channelStatus(),
                request.failedTime());
    }

    public static OrderCloseExpiredCommand toCloseExpiredCommand(OrderCloseExpiredFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderCloseExpiredCommand(OrderNoCodec.toDomain(request.getOrderNo()), request.getReason());
    }

    public static OrderCloseExpiredCommand toCloseExpiredCommand(OrderCloseExpiredRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderCloseExpiredCommand(OrderNoCodec.toDomain(request.orderNo()), request.reason());
    }

    public static OrderDetailFacadeResponse toDetailFacadeResponse(OrderDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        List<OrderItemFacadeResponse> items = dto.getItems() == null
                ? List.of()
                : dto.getItems().stream().map(OrderInterfaceAssembler::toItemFacadeResponse).toList();
        return new OrderDetailFacadeResponse(
                dto.getOrderNo(),
                dto.getUserId(),
                dto.getOrderStatus(),
                dto.getPayStatus(),
                dto.getInventoryStatus(),
                dto.getPaymentNo(),
                dto.getReservationNo(),
                dto.getCurrencyCode(),
                dto.getTotalAmount(),
                dto.getPayableAmount(),
                dto.getCancelReason(),
                dto.getCloseReason(),
                dto.getCreatedAt(),
                dto.getExpiredAt(),
                items,
                dto.getPaymentSnapshot(),
                dto.getInventorySnapshot(),
                dto.getPaidAt(),
                dto.getClosedAt());
    }

    public static OrderPageFacadeResponse toPageFacadeResponse(OrderPageResult dto) {
        if (dto == null) {
            return null;
        }
        List<OrderSummaryFacadeResponse> records = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(OrderInterfaceAssembler::toSummaryFacadeResponse).toList();
        return new OrderPageFacadeResponse(records, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }

    public static OrderOutboxDeadLetterReplayResponse toReplayResponse(OrderOutboxDeadLetterReplayResult dto) {
        if (dto == null) {
            return null;
        }
        return new OrderOutboxDeadLetterReplayResponse(dto.deadLetterId(), dto.replayStatus(), dto.message());
    }

    public static OrderSummaryResponse toSummaryResponse(OrderSummaryDTO dto) {
        if (dto == null) {
            return null;
        }
        return new OrderSummaryResponse(
                dto.getId(),
                dto.getOrderNo(),
                dto.getUserId(),
                dto.getOrderStatus(),
                dto.getPayStatus(),
                dto.getInventoryStatus(),
                dto.getPaymentNo(),
                dto.getReservationNo(),
                dto.getCurrencyCode(),
                dto.getTotalAmount(),
                dto.getPayableAmount(),
                dto.getCancelReason(),
                dto.getCloseReason(),
                dto.getCreatedAt(),
                dto.getExpiredAt());
    }

    public static OrderDetailResponse toDetailResponse(OrderDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        List<OrderItemResponse> items = dto.getItems() == null
                ? List.of()
                : dto.getItems().stream().map(OrderInterfaceAssembler::toItemResponse).toList();
        return new OrderDetailResponse(
                dto.getId(),
                dto.getOrderNo(),
                dto.getUserId(),
                dto.getOrderStatus(),
                dto.getPayStatus(),
                dto.getInventoryStatus(),
                dto.getPaymentNo(),
                dto.getReservationNo(),
                dto.getCurrencyCode(),
                dto.getTotalAmount(),
                dto.getPayableAmount(),
                dto.getCancelReason(),
                dto.getCloseReason(),
                dto.getCreatedAt(),
                dto.getExpiredAt(),
                items,
                dto.getPaymentSnapshot(),
                dto.getInventorySnapshot(),
                dto.getPaidAt(),
                dto.getClosedAt());
    }

    public static OrderPageResponse toPageResponse(OrderPageResult dto) {
        if (dto == null) {
            return null;
        }
        List<OrderSummaryResponse> records = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(OrderInterfaceAssembler::toSummaryResponse).toList();
        return new OrderPageResponse(records, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }

    private static OrderItemResponse toItemResponse(OrderItemDTO dto) {
        return new OrderItemResponse(
                dto.getSkuId(),
                dto.getSkuName(),
                dto.getImageUrl(),
                dto.getQuantity(),
                dto.getSalePrice(),
                dto.getLineAmount());
    }

    private static OrderItemFacadeResponse toItemFacadeResponse(OrderItemDTO dto) {
        return new OrderItemFacadeResponse(
                dto.getSkuId(),
                dto.getSkuName(),
                dto.getImageUrl(),
                dto.getQuantity(),
                dto.getSalePrice(),
                dto.getLineAmount());
    }

    private static OrderSummaryFacadeResponse toSummaryFacadeResponse(OrderSummaryDTO dto) {
        return new OrderSummaryFacadeResponse(
                dto.getOrderNo(),
                dto.getUserId(),
                dto.getOrderStatus(),
                dto.getPayStatus(),
                dto.getInventoryStatus(),
                dto.getPaymentNo(),
                dto.getReservationNo(),
                dto.getCurrencyCode(),
                dto.getTotalAmount(),
                dto.getPayableAmount(),
                dto.getCancelReason(),
                dto.getCloseReason(),
                dto.getCreatedAt(),
                dto.getExpiredAt());
    }
}
