package com.github.thundax.bacon.upms.application.audit;

import com.github.thundax.bacon.common.application.page.PageQuery;
import lombok.Getter;

@Getter
public class SysLogPageQuery extends PageQuery {

    private final String module;
    private final String eventType;
    private final String result;
    private final String operatorName;

    public SysLogPageQuery(
            String module, String eventType, String result, String operatorName, Integer pageNo, Integer pageSize) {
        super(pageNo, pageSize);
        this.module = module;
        this.eventType = eventType;
        this.result = result;
        this.operatorName = operatorName;
    }
}
