package com.github.thundax.bacon.inventory.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveCommandDTO {

    @NotEmpty
    private List<@Valid InventoryReservationItemDTO> items;
}
