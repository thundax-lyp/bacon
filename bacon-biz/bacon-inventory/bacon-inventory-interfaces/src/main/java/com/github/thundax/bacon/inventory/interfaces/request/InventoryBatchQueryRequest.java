package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchQueryRequest {

    @NotEmpty
    private Set<@NotNull @Positive Long> skuIds;
}
