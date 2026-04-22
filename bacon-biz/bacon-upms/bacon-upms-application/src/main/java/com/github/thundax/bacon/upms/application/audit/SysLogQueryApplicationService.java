package com.github.thundax.bacon.upms.application.audit;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.upms.application.assembler.SysLogAssembler;
import com.github.thundax.bacon.upms.application.dto.SysLogDTO;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SysLogQueryApplicationService {

    private final SysLogRepository sysLogRepository;

    public SysLogQueryApplicationService(SysLogRepository sysLogRepository) {
        this.sysLogRepository = sysLogRepository;
    }

    public PageResult<SysLogDTO> page(SysLogPageQuery query) {
        return new PageResult<>(
                sysLogRepository
                        .page(
                                query.getModule(),
                                query.getEventType(),
                                query.getResult(),
                                query.getOperatorName(),
                                query.getPageNo(),
                                query.getPageSize())
                        .stream()
                        .map(SysLogAssembler::toDto)
                        .toList(),
                sysLogRepository.count(query.getModule(), query.getEventType(), query.getResult(), query.getOperatorName()),
                query.getPageNo(),
                query.getPageSize());
    }

    public SysLogDTO getById(SysLogId logId) {
        return SysLogAssembler.toDto(sysLogRepository
                .findById(logId)
                .orElseThrow(() -> new NotFoundException("Sys log not found: " + logId.value())));
    }
}
