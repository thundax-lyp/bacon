package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.common.id.codec.OperatorIdCodec;
import com.github.thundax.bacon.upms.application.dto.SysLogDTO;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;

public final class SysLogAssembler {

    private SysLogAssembler() {}

    public static SysLogDTO toDto(SysLogRecord record) {
        return new SysLogDTO(
                record.getId(),
                record.getTraceId(),
                record.getRequestId(),
                record.getModule(),
                record.getAction(),
                record.getEventType(),
                record.getResult(),
                OperatorIdCodec.toValue(record.getOperatorId()),
                record.getOperatorName(),
                record.getClientIp(),
                record.getRequestUri(),
                record.getHttpMethod(),
                record.getCostMs(),
                record.getErrorMessage(),
                record.getOccurredAt());
    }
}
