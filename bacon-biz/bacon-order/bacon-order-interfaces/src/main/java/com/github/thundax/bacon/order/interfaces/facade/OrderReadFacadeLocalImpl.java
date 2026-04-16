package com.github.thundax.bacon.order.interfaces.facade;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.api.query.OrderPageQuery;
import com.github.thundax.bacon.order.application.codec.OrderIdCodec;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class OrderReadFacadeLocalImpl implements OrderReadFacade {

    private final OrderQueryApplicationService orderQueryService;

    public OrderReadFacadeLocalImpl(OrderQueryApplicationService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @Override
    public OrderDetailDTO getById(Long orderId) {
        BaconContextHolder.requireTenantId();
        return orderQueryService.getById(OrderIdCodec.toDomain(orderId));
    }

    @Override
    public OrderDetailDTO getByOrderNo(String orderNo) {
        BaconContextHolder.requireTenantId();
        return orderQueryService.getByOrderNo(OrderNoCodec.toDomain(orderNo));
    }

    @Override
    public OrderPageResultDTO pageOrders(OrderPageQuery query) {
        return orderQueryService.pageOrders(
                UserIdCodec.toDomain(query.getUserId()),
                OrderNoCodec.toDomain(query.getOrderNo()),
                query.getOrderStatus() == null ? null : OrderStatus.from(query.getOrderStatus()),
                query.getPayStatus() == null ? null : PayStatus.from(query.getPayStatus()),
                query.getInventoryStatus() == null ? null : InventoryStatus.from(query.getInventoryStatus()),
                query.getCreatedAtFrom(),
                query.getCreatedAtTo(),
                query.getPageNo(),
                query.getPageSize());
    }
}
