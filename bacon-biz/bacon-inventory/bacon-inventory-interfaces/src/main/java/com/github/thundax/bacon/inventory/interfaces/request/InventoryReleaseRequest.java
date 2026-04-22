package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 库存释放请求。
 */
public record InventoryReleaseRequest(
        @NotBlank @Size(max = 64) String orderNo, @NotBlank @Size(max = 255) String reason) {}
