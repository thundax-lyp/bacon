package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostCode;
import lombok.Getter;

@Getter
public class PostPageQuery extends PageQuery {

    private final PostCode code;
    private final String name;
    private final DepartmentId departmentId;
    private final PostStatus status;

    public PostPageQuery(
            PostCode code, String name, DepartmentId departmentId, PostStatus status, Integer pageNo, Integer pageSize) {
        super(pageNo, pageSize);
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
        this.status = status;
    }
}
