package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import lombok.Getter;

@Getter
public class ResourcePageQuery extends PageQuery {

    private final ResourceCode code;
    private final String name;
    private final ResourceType resourceType;
    private final ResourceStatus status;

    public ResourcePageQuery(
            ResourceCode code,
            String name,
            ResourceType resourceType,
            ResourceStatus status,
            Integer pageNo,
            Integer pageSize) {
        super(pageNo, pageSize);
        this.code = code;
        this.name = name;
        this.resourceType = resourceType;
        this.status = status;
    }
}
