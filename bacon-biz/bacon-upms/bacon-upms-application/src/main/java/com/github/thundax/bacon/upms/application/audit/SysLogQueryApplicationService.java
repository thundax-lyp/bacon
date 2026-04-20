package com.github.thundax.bacon.upms.application.audit;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.upms.application.assembler.SysLogAssembler;
import com.github.thundax.bacon.upms.application.dto.SysLogDTO;
import com.github.thundax.bacon.upms.application.result.PageResult;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SysLogQueryApplicationService {

    private final SysLogRepository sysLogRepository;

    public SysLogQueryApplicationService(SysLogRepository sysLogRepository) {
        this.sysLogRepository = sysLogRepository;
    }

    public PageResult<SysLogDTO> page(
            String module, String eventType, String result, String operatorName, Integer pageNo, Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        // 日志分页统一先归一化页码参数，避免调用方传入 0/负数时把仓储查询语义拉偏。
        return new PageResult<>(
                sysLogRepository
                        .page(module, eventType, result, operatorName, normalizedPageNo, normalizedPageSize)
                        .stream()
                        .map(SysLogAssembler::toDto)
                        .toList(),
                sysLogRepository.count(module, eventType, result, operatorName),
                normalizedPageNo,
                normalizedPageSize);
    }

    public SysLogDTO getLogById(SysLogId logId) {
        return SysLogAssembler.toDto(sysLogRepository
                .findById(logId)
                .orElseThrow(() -> new NotFoundException("Sys log not found: " + logId.value())));
    }
}
