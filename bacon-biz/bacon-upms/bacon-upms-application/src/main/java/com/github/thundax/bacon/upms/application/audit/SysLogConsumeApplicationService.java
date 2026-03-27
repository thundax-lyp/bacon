package com.github.thundax.bacon.upms.application.audit;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import org.springframework.stereotype.Service;

import static java.lang.Long.parseLong;

@Service
public class SysLogConsumeApplicationService {

    private final SysLogRepository sysLogRepository;

    public SysLogConsumeApplicationService(SysLogRepository sysLogRepository) {
        this.sysLogRepository = sysLogRepository;
    }

    public void consume(SysLogDTO sysLogDTO) {
        SysLogRecord sysLogRecord = new SysLogRecord(
                null,
                sysLogDTO.getTenantId(),
                sysLogDTO.getTraceId(),
                sysLogDTO.getRequestId(),
                sysLogDTO.getModule(),
                sysLogDTO.getAction(),
                sysLogDTO.getEventType().name(),
                sysLogDTO.getResult().name(),
                parseLong(sysLogDTO.getOperatorId()),
                sysLogDTO.getOperatorName(),
                sysLogDTO.getClientIp(),
                sysLogDTO.getRequestUri(),
                sysLogDTO.getHttpMethod(),
                sysLogDTO.getCostMs(),
                sysLogDTO.getErrorMessage(),
                sysLogDTO.getOccurredAt()
        );
        // 系统日志同时落数据库和文件：数据库用于检索聚合，文件用于本地排障和最低成本保留。
        sysLogRepository.saveToDatabase(sysLogRecord);
        sysLogRepository.saveToFile(sysLogRecord);
    }
}
