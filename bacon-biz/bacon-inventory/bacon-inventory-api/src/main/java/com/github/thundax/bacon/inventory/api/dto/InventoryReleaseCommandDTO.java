package com.github.thundax.bacon.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存释放命令对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReleaseCommandDTO {

    /** 释放原因。 */
    @NotBlank
    private String reason;
}
