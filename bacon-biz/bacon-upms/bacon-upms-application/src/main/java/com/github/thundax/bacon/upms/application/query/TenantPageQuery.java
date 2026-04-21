package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import lombok.Getter;

@Getter
public class TenantPageQuery extends PageQuery {

    private final String name;
    private final TenantStatus status;

    public TenantPageQuery(String name, TenantStatus status, Integer pageNo, Integer pageSize) {
        super(pageNo, pageSize);
        this.name = name;
        this.status = status;
    }
}
