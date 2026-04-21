package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.core.result.PageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import lombok.Getter;

@Getter
public class RolePageQuery extends PageQuery {

    private final RoleCode code;
    private final String name;
    private final RoleType roleType;
    private final RoleStatus status;

    public RolePageQuery(RoleCode code, String name, RoleType roleType, RoleStatus status, Integer pageNo, Integer pageSize) {
        super(pageNo, pageSize);
        this.code = code;
        this.name = name;
        this.roleType = roleType;
        this.status = status;
    }
}
