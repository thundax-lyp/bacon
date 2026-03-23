package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SysLogRepositoryImpl implements SysLogRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final Path SYS_LOG_FILE = Path.of("logs", "upms-sys-log.log");

    private final InMemoryUpmsStore inMemoryUpmsStore;
    private final AtomicLong logIdSequence = new AtomicLong(1L);

    public SysLogRepositoryImpl(InMemoryUpmsStore inMemoryUpmsStore) {
        this.inMemoryUpmsStore = inMemoryUpmsStore;
    }

    @Override
    public void saveToDatabase(SysLogRecord sysLogRecord) {
        Long id = sysLogRecord.getId() == null ? logIdSequence.getAndIncrement() : sysLogRecord.getId();
        SysLogRecord persistedRecord = new SysLogRecord(
                id,
                sysLogRecord.getTenantId(),
                sysLogRecord.getTraceId(),
                sysLogRecord.getRequestId(),
                sysLogRecord.getModule(),
                sysLogRecord.getAction(),
                sysLogRecord.getEventType(),
                sysLogRecord.getResult(),
                sysLogRecord.getOperatorId(),
                sysLogRecord.getOperatorName(),
                sysLogRecord.getClientIp(),
                sysLogRecord.getRequestUri(),
                sysLogRecord.getHttpMethod(),
                sysLogRecord.getCostMs(),
                sysLogRecord.getErrorMessage(),
                sysLogRecord.getOccurredAt()
        );
        inMemoryUpmsStore.getSysLogs().put(id, persistedRecord);
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
                    StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write sys log file", ex);
        }
    }

    private String formatLine(SysLogRecord sysLogRecord) {
        return String.join("|",
                nullSafe(sysLogRecord.getTraceId()),
                nullSafe(sysLogRecord.getRequestId()),
                nullSafe(sysLogRecord.getModule()),
                nullSafe(sysLogRecord.getAction()),
                nullSafe(sysLogRecord.getEventType()),
                nullSafe(sysLogRecord.getResult()),
                nullSafe(sysLogRecord.getTenantId()),
                nullSafe(sysLogRecord.getOperatorId()),
                nullSafe(sysLogRecord.getOperatorName()),
                nullSafe(sysLogRecord.getClientIp()),
                nullSafe(sysLogRecord.getRequestUri()),
                nullSafe(sysLogRecord.getHttpMethod()),
                nullSafe(sysLogRecord.getCostMs()),
                nullSafe(sysLogRecord.getErrorMessage()),
                sysLogRecord.getOccurredAt() == null ? "" : FORMATTER.format(sysLogRecord.getOccurredAt())
        ) + System.lineSeparator();
    }

    private String nullSafe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
