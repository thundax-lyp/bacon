package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.SysLogRecordDO;

public final class SysLogRecordPersistenceAssembler {

    private SysLogRecordPersistenceAssembler() {}

    public static SysLogRecordDO toDataObject(SysLogRecord sysLogRecord) {
        return new SysLogRecordDO(
                sysLogRecord.getId(),
                sysLogRecord.getTenantId(),
                sysLogRecord.getTraceId(),
                sysLogRecord.getRequestId(),
                sysLogRecord.getModule(),
                sysLogRecord.getAction(),
                sysLogRecord.getEventType(),
                sysLogRecord.getResult(),
                sysLogRecord.getOperatorId(),
                sysLogRecord.getOperatorName(),
                sysLogRecord.getClientIp(),
                sysLogRecord.getRequestUri(),
                sysLogRecord.getHttpMethod(),
                sysLogRecord.getCostMs(),
                sysLogRecord.getErrorMessage(),
                sysLogRecord.getOccurredAt());
    }

    public static SysLogRecord toDomain(SysLogRecordDO dataObject) {
        return SysLogRecord.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getTraceId(),
                dataObject.getRequestId(),
                dataObject.getModule(),
                dataObject.getAction(),
                dataObject.getEventType(),
                dataObject.getResult(),
                dataObject.getOperatorId(),
                dataObject.getOperatorName(),
                dataObject.getClientIp(),
                dataObject.getRequestUri(),
                dataObject.getHttpMethod(),
                dataObject.getCostMs(),
                dataObject.getErrorMessage(),
                dataObject.getOccurredAt());
    }
}
