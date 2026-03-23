package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.SysLogRecord;

public interface SysLogRepository {

    void saveToDatabase(SysLogRecord sysLogRecord);

    void saveToFile(SysLogRecord sysLogRecord);
}
