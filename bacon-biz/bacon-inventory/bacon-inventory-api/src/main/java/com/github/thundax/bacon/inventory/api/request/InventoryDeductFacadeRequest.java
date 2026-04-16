package com.github.thundax.bacon.inventory.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存扣减门面请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDeductFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;
}
