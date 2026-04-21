package com.github.thundax.bacon.common.application.page;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import lombok.Getter;

/**
 * 通用分页查询参数。
 */
@Getter
public class PageQuery {

    /** 请求页码。 */
    private Integer pageNo;
    /** 请求每页大小。 */
    private Integer pageSize;

    public PageQuery(Integer pageNo, Integer pageSize) {
        this.pageNo = PageParamNormalizer.normalizePageNo(pageNo);
        this.pageSize = PageParamNormalizer.normalizePageSize(pageSize);
    }
}
