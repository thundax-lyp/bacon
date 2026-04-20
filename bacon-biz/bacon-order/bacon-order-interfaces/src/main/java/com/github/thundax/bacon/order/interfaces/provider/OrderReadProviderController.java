package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.interfaces.assembler.OrderFacadeResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/providers/order")
@Tag(name = "Inner-Order-Management", description = "Order 域内部 Provider 接口")
public class OrderReadProviderController {

    private final OrderQueryApplicationService orderQueryService;

    public OrderReadProviderController(OrderQueryApplicationService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/{orderNo}")
    public OrderDetailFacadeResponse getByOrderNo(@PathVariable("orderNo") @NotBlank String orderNo) {
        return OrderFacadeResponseAssembler.fromDetailDto(orderQueryService.getByOrderNo(OrderNoCodec.toDomain(orderNo)));
    }

    @GetMapping
    public OrderPageFacadeResponse page(@Valid OrderPageFacadeRequest request) {
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
