package com.github.thundax.bacon.upms.application.audit;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.upms.api.dto.SysLogDTO;
import com.github.thundax.bacon.upms.api.dto.SysLogPageResultDTO;
import com.github.thundax.bacon.upms.api.dto.SysLogQueryDTO;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SysLogQueryApplicationService {

    private final SysLogRepository sysLogRepository;

    public SysLogQueryApplicationService(SysLogRepository sysLogRepository) {
        this.sysLogRepository = sysLogRepository;
    }

    public SysLogPageResultDTO pageLogs(SysLogQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        // 日志分页统一先归一化页码参数，避免调用方传入 0/负数时把仓储查询语义拉偏。
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
                record.getOperatorId() == null ? null : String.valueOf(record.getOperatorId().value()), record.getOperatorName(),
                record.getClientIp(), record.getRequestUri(),
                record.getHttpMethod(), record.getCostMs(), record.getErrorMessage(), record.getOccurredAt());
    }

}
