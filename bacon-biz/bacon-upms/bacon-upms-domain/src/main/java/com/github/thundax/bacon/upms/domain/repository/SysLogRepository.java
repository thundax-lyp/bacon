package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import java.util.List;
import java.util.Optional;

public interface SysLogRepository {

    void saveToDatabase(SysLogRecord sysLogRecord);

    void saveToFile(SysLogRecord sysLogRecord);

    Optional<SysLogRecord> findById(SysLogId logId);

    List<SysLogRecord> pageLogs(
            String module, String eventType, String result, String operatorName, int pageNo, int pageSize);

    long countLogs(String module, String eventType, String result, String operatorName);
}
