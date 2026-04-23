package com.github.thundax.bacon.inventory.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存释放门面请求。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReleaseFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;

    @NotBlank
    @Size(max = 255)
    private String reason;
}
