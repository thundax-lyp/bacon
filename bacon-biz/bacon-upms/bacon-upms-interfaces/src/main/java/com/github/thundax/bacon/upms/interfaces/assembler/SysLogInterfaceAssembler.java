package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.upms.application.audit.SysLogPageQuery;
import com.github.thundax.bacon.upms.interfaces.request.SysLogPageRequest;

public final class SysLogInterfaceAssembler {

    private SysLogInterfaceAssembler() {}

    public static SysLogPageQuery toPageQuery(SysLogPageRequest request) {
        return new SysLogPageQuery(
                request.getModule(),
                request.getEventType() == null ? null : request.getEventType().name(),
                request.getResult(),
                request.getOperatorName(),
                request.getPageNo(),
                request.getPageSize());
    }
}
