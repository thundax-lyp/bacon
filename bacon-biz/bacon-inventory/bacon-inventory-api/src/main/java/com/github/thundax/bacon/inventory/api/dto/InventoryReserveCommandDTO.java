package com.github.thundax.bacon.inventory.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占命令对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveCommandDTO {

    /** 预占明细列表。 */
    @NotEmpty
    private List<@Valid InventoryReservationItemDTO> items;
}
