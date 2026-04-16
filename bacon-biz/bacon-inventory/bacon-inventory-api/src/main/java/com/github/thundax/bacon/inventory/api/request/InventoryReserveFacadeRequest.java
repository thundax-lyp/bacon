package com.github.thundax.bacon.inventory.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占门面请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;

    @NotEmpty
    private List<@Valid InventoryReservationItemFacadeRequest> items;
}
