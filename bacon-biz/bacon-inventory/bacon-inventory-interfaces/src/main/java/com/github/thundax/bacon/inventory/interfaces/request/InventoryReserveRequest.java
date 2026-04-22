package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 库存预占请求。
 */
public record InventoryReserveRequest(
        @NotBlank @Size(max = 64) String orderNo, @NotEmpty List<@Valid InventoryReservationItemRequest> items) {}
