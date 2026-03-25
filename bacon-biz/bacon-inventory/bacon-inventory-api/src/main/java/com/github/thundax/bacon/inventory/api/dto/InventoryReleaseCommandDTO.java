package com.github.thundax.bacon.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReleaseCommandDTO {

    @NotBlank
    private String reason;
}
