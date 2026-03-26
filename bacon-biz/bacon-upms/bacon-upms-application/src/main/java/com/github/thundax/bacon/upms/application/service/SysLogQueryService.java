package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.upms.api.dto.SysLogDTO;
import com.github.thundax.bacon.upms.api.dto.SysLogPageResultDTO;
import com.github.thundax.bacon.upms.api.dto.SysLogQueryDTO;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SysLogQueryService {

    private final SysLogRepository sysLogRepository;

    public SysLogQueryService(SysLogRepository sysLogRepository) {
        this.sysLogRepository = sysLogRepository;
    }

    public SysLogPageResultDTO pageLogs(SysLogQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new SysLogPageResultDTO(
                sysLogRepository.pageLogs(query.getTenantId(), query.getModule(), query.getEventType(),
                                query.getResult(), query.getOperatorName(), pageNo, pageSize).stream()
                        .map(this::toDto)
                        .toList(),
                sysLogRepository.countLogs(query.getTenantId(), query.getModule(), query.getEventType(),
                        query.getResult(), query.getOperatorName()),
                pageNo,
                pageSize
        );
    }

    public SysLogDTO getLogById(Long logId) {
        return toDto(sysLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Sys log not found: " + logId)));
    }

    private SysLogDTO toDto(SysLogRecord record) {
        return new SysLogDTO(record.getId(), record.getTenantId(), record.getTraceId(), record.getRequestId(),
                record.getModule(), record.getAction(), record.getEventType(), record.getResult(),
                record.getOperatorId(), record.getOperatorName(), record.getClientIp(), record.getRequestUri(),
                record.getHttpMethod(), record.getCostMs(), record.getErrorMessage(), record.getOccurredAt());
    }

}
