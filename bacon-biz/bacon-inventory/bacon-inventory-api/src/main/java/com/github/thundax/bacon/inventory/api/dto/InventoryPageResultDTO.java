package com.github.thundax.bacon.inventory.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存分页结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPageResultDTO {

    /** 当前页记录。 */
    private List<InventoryStockDTO> records;
    /** 总记录数。 */
    private long total;
    /** 页码。 */
    private int pageNo;
    /** 每页条数。 */
    private int pageSize;
}
