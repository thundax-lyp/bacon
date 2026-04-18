package com.github.thundax.bacon.order.interfaces.facade;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.interfaces.assembler.OrderFacadeResponseAssembler;
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
    public OrderDetailFacadeResponse getByOrderNo(OrderDetailFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return OrderFacadeResponseAssembler.fromDetailDto(
                orderQueryService.getByOrderNo(OrderNoCodec.toDomain(request.getOrderNo())));
    }

    @Override
    public OrderPageFacadeResponse page(OrderPageFacadeRequest request) {
        return OrderFacadeResponseAssembler.fromPageDto(orderQueryService.page(
                UserIdCodec.toDomain(request.getUserId()),
                OrderNoCodec.toDomain(request.getOrderNo()),
                request.getOrderStatus() == null ? null : OrderStatus.from(request.getOrderStatus()),
                request.getPayStatus() == null ? null : PayStatus.from(request.getPayStatus()),
                request.getInventoryStatus() == null ? null : InventoryStatus.from(request.getInventoryStatus()),
                request.getCreatedAtFrom(),
                request.getCreatedAtTo(),
                request.getPageNo(),
                request.getPageSize()));
    }
}
