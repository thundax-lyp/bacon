package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class SysLogRepositoryImpl implements SysLogRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final Path SYS_LOG_FILE = Path.of("logs", "upms-sys-log.log");

    private final SysLogPersistenceSupport support;

    public SysLogRepositoryImpl(SysLogPersistenceSupport support) {
        this.support = support;
    }

    @Override
    public void saveToDatabase(SysLogRecord sysLogRecord) {
        support.saveSysLog(sysLogRecord);
    }

    @Override
    public void saveToFile(SysLogRecord sysLogRecord) {
        try {
            Path parent = SYS_LOG_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    SYS_LOG_FILE,
                    formatLine(sysLogRecord),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write sys log file", ex);
        }
    }

    @Override
    public Optional<SysLogRecord> findById(Long logId) {
        return support.findSysLogById(logId);
    }

    @Override
    public List<SysLogRecord> pageLogs(String module, String eventType, String result, String operatorName, int pageNo, int pageSize) {
        return support.listSysLogs(module, eventType, result, operatorName, pageNo, pageSize);
    }

    @Override
    public long countLogs(String module, String eventType, String result, String operatorName) {
        return support.countSysLogs(module, eventType, result, operatorName);
    }

    private String formatLine(SysLogRecord sysLogRecord) {
        return String.join(
                        "|",
                        nullSafe(sysLogRecord.getTraceId()),
                        nullSafe(sysLogRecord.getRequestId()),
                        nullSafe(sysLogRecord.getModule()),
                        nullSafe(sysLogRecord.getAction()),
                        nullSafe(sysLogRecord.getEventType()),
                        nullSafe(sysLogRecord.getResult()),
                        "",
                        nullSafe(sysLogRecord.getOperatorId()),
                        nullSafe(sysLogRecord.getOperatorName()),
                        nullSafe(sysLogRecord.getClientIp()),
                        nullSafe(sysLogRecord.getRequestUri()),
                        nullSafe(sysLogRecord.getHttpMethod()),
                        nullSafe(sysLogRecord.getCostMs()),
                        nullSafe(sysLogRecord.getErrorMessage()),
                        sysLogRecord.getOccurredAt() == null ? "" : FORMATTER.format(sysLogRecord.getOccurredAt()))
                + System.lineSeparator();
    }

    private String nullSafe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
