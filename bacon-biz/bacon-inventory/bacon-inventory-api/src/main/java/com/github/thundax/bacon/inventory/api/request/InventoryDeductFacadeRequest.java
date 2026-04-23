package com.github.thundax.bacon.inventory.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存扣减门面请求。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDeductFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;
}
