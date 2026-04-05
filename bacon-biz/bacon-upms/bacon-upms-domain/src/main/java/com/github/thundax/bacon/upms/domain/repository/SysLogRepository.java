package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import java.util.List;
import java.util.Optional;

public interface SysLogRepository {

    void saveToDatabase(SysLogRecord sysLogRecord);

    void saveToFile(SysLogRecord sysLogRecord);

    Optional<SysLogRecord> findById(Long logId);

    List<SysLogRecord> pageLogs(Long tenantId, String module, String eventType, String result, String operatorName,
                                int pageNo, int pageSize);

    long countLogs(Long tenantId, String module, String eventType, String result, String operatorName);
}
