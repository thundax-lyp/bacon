package com.github.thundax.bacon.common.application.page;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页结果对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页记录。 */
    private List<T> records;
    /** 总记录数。 */
    private long total;
    /** 当前页码。 */
    private int pageNo;
    /** 每页大小。 */
    private int pageSize;
}
