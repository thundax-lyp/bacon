package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

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

    @Override
    public Optional<SysLogRecord> findById(Long logId) {
        return Optional.ofNullable(inMemoryUpmsStore.getSysLogs().get(logId));
    }

    @Override
    public List<SysLogRecord> pageLogs(String tenantId, String module, String eventType, String result,
                                       String operatorName, int pageNo, int pageSize) {
        return filteredLogs(tenantId, module, eventType, result, operatorName).stream()
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    @Override
    public long countLogs(String tenantId, String module, String eventType, String result, String operatorName) {
        return filteredLogs(tenantId, module, eventType, result, operatorName).size();
    }

    private List<SysLogRecord> filteredLogs(String tenantId, String module, String eventType, String result,
                                            String operatorName) {
        return inMemoryUpmsStore.getSysLogs().values().stream()
                .filter(record -> tenantId == null || tenantId.equals(record.getTenantId()))
                .filter(record -> module == null || module.equalsIgnoreCase(record.getModule()))
                .filter(record -> eventType == null || eventType.equalsIgnoreCase(record.getEventType()))
                .filter(record -> result == null || result.equalsIgnoreCase(record.getResult()))
                .filter(record -> operatorName == null || (record.getOperatorName() != null
                        && record.getOperatorName().contains(operatorName)))
                .sorted(Comparator.comparing(SysLogRecord::getId).reversed())
                .toList();
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
