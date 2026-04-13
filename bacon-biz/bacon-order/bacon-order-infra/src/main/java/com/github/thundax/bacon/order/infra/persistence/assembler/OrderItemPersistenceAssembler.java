package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderItemDO;
import org.springframework.stereotype.Component;

@Component
public class OrderItemPersistenceAssembler {

    public OrderItemDO toDataObject(OrderItem item) {
        return new OrderItemDO(
                item.getId(),
                BaconContextHolder.requireTenantId(),
                item.getOrderId() == null ? null : item.getOrderId().value(),
                item.getSkuId() == null ? null : item.getSkuId().value(),
                item.getSkuName(),
                item.getImageUrl(),
                item.getQuantity(),
                item.getSalePrice() == null ? null : item.getSalePrice().value(),
                item.getLineAmount() == null ? null : item.getLineAmount().value());
    }

    public OrderItem toDomain(OrderItemDO dataObject) {
        return OrderItem.reconstruct(
                dataObject.getId(),
                dataObject.getOrderId() == null ? null : OrderId.of(dataObject.getOrderId()),
                dataObject.getSkuId() == null ? null : SkuId.of(dataObject.getSkuId()),
                dataObject.getSkuName(),
                dataObject.getImageUrl(),
                dataObject.getQuantity(),
                dataObject.getSalePrice() == null
                        ? null
                        : Money.of(dataObject.getSalePrice(), CurrencyCode.UNSPECIFIED),
                dataObject.getLineAmount() == null
                        ? null
                        : Money.of(dataObject.getLineAmount(), CurrencyCode.UNSPECIFIED));
    }
}
