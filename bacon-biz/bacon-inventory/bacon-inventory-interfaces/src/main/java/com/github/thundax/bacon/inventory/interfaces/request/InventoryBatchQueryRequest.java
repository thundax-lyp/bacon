package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchQueryRequest {

    @NotEmpty
    private Set<@NotNull @Positive Long> skuIds;
}
