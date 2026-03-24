package com.github.thundax.bacon.upms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantPageResultDTO {

    private List<TenantDTO> records;
    private long total;
    private int pageNo;
    private int pageSize;
}
