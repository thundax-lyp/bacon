package com.github.thundax.bacon.upms.application.audit;

import com.github.thundax.bacon.common.id.codec.OperatorIdCodec;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SysLogConsumeApplicationService {

    private static final String SYS_LOG_ID_BIZ_TAG = "upms-sys-log-id";

    private final SysLogRepository sysLogRepository;
    private final IdGenerator idGenerator;

    public SysLogConsumeApplicationService(SysLogRepository sysLogRepository, IdGenerator idGenerator) {
        this.sysLogRepository = sysLogRepository;
        this.idGenerator = idGenerator;
    }

    public void consume(SysLogDTO sysLogDTO) {
        SysLogRecord sysLogRecord = SysLogRecord.create(
                idGenerator.nextId(SYS_LOG_ID_BIZ_TAG),
                sysLogDTO.getTraceId(),
                sysLogDTO.getRequestId(),
                sysLogDTO.getModule(),
                sysLogDTO.getAction(),
                sysLogDTO.getEventType().name(),
                sysLogDTO.getResult().name(),
                OperatorIdCodec.toDomain(sysLogDTO.getOperatorId()),
                sysLogDTO.getOperatorName(),
                sysLogDTO.getClientIp(),
                sysLogDTO.getRequestUri(),
                sysLogDTO.getHttpMethod(),
                sysLogDTO.getCostMs(),
                sysLogDTO.getErrorMessage(),
                sysLogDTO.getOccurredAt());
        // 系统日志同时落数据库和文件：数据库用于检索聚合，文件用于本地排障和最低成本保留。
        sysLogRepository.saveToDatabase(sysLogRecord);
        sysLogRepository.saveToFile(sysLogRecord);
    }
}
