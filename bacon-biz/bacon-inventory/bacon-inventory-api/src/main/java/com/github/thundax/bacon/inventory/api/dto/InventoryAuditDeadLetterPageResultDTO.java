package com.github.thundax.bacon.inventory.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditDeadLetterPageResultDTO {

    private List<InventoryAuditDeadLetterDTO> records;
    private long total;
    private int pageNo;
    private int pageSize;
}
