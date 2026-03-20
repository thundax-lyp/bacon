package com.github.thundax.bacon.order.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageResultDTO {

    private List<OrderSummaryDTO> records;
    private long total;
    private int pageNo;
    private int pageSize;
}
