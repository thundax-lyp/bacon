package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageResultDTO {

    private List<OrderSummaryDTO> records;
    private long total;
    private int pageNo;
    private int pageSize;
}
