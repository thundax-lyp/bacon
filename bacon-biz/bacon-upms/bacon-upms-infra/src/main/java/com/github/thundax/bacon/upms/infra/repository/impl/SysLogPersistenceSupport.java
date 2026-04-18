package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.SysLogRecordPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.SysLogRecordDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.SysLogRecordMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class SysLogPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final SysLogRecordMapper sysLogRecordMapper;

    SysLogPersistenceSupport(SysLogRecordMapper sysLogRecordMapper) {
        this.sysLogRecordMapper = sysLogRecordMapper;
    }

    void saveSysLog(SysLogRecord sysLogRecord) {
        SysLogRecordDO dataObject = SysLogRecordPersistenceAssembler.toDataObject(sysLogRecord);
        if (dataObject.getId() == null) {
            sysLogRecordMapper.insert(dataObject);
        } else {
            sysLogRecordMapper.updateById(dataObject);
        }
    }

    Optional<SysLogRecord> findSysLogById(SysLogId logId) {
        return Optional.ofNullable(sysLogRecordMapper.selectById(logId.value()))
                .map(SysLogRecordPersistenceAssembler::toDomain);
    }

    List<SysLogRecord> listSysLogs(
            String module, String eventType, String result, String operatorName, int pageNo, int pageSize) {
        return sysLogRecordMapper
                .selectList(Wrappers.<SysLogRecordDO>lambdaQuery()
                        .eq(hasText(module), SysLogRecordDO::getModule, trim(module))
                        .eq(hasText(eventType), SysLogRecordDO::getEventType, trim(eventType))
                        .eq(hasText(result), SysLogRecordDO::getResult, trim(result))
                        .like(hasText(operatorName), SysLogRecordDO::getOperatorName, operatorName)
                        .orderByDesc(SysLogRecordDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(SysLogRecordPersistenceAssembler::toDomain)
                .toList();
    }

    long count(String module, String eventType, String result, String operatorName) {
        return Optional.ofNullable(sysLogRecordMapper.selectCount(Wrappers.<SysLogRecordDO>lambdaQuery()
                        .eq(hasText(module), SysLogRecordDO::getModule, trim(module))
                        .eq(hasText(eventType), SysLogRecordDO::getEventType, trim(eventType))
                        .eq(hasText(result), SysLogRecordDO::getResult, trim(result))
                        .like(hasText(operatorName), SysLogRecordDO::getOperatorName, operatorName)))
                .orElse(0L);
    }
}
