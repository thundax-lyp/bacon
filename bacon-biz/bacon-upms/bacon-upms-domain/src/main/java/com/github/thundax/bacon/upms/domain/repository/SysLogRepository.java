package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import java.util.List;
import java.util.Optional;

public interface SysLogRepository {

    Optional<SysLogRecord> findById(SysLogId logId);

    List<SysLogRecord> page(
            String module, String eventType, String result, String operatorName, int pageNo, int pageSize);

    long count(String module, String eventType, String result, String operatorName);

    void insertToDatabase(SysLogRecord sysLogRecord);

    void insertToFile(SysLogRecord sysLogRecord);
}
