package com.github.thundax.bacon.upms.application.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.upms.domain.model.entity.SysLogRecord;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SysLogQueryApplicationServiceTest {

    @Test
    void shouldPageWithNormalizedQueryContract() {
        SysLogRepository repository = mock(SysLogRepository.class);
        when(repository.page("UPMS", "QUERY", "SUCCESS", "alice", 1, 200))
                .thenReturn(List.of(sysLog(1L)));
        when(repository.count("UPMS", "QUERY", "SUCCESS", "alice")).thenReturn(1L);

        SysLogQueryApplicationService service = new SysLogQueryApplicationService(repository);

        var result = service.page(new SysLogPageQuery("UPMS", "QUERY", "SUCCESS", "alice", 0, 500));

        assertEquals(1, result.getPageNo());
        assertEquals(200, result.getPageSize());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(repository).page("UPMS", "QUERY", "SUCCESS", "alice", 1, 200);
    }

    @Test
    void shouldGetById() {
        SysLogRepository repository = mock(SysLogRepository.class);
        when(repository.findById(SysLogId.of(1L))).thenReturn(Optional.of(sysLog(1L)));

        SysLogQueryApplicationService service = new SysLogQueryApplicationService(repository);

        var result = service.getById(SysLogId.of(1L));

        assertEquals(1L, result.getId());
        assertEquals("UPMS", result.getModule());
    }

    @Test
    void shouldThrowWhenSysLogNotFound() {
        SysLogRepository repository = mock(SysLogRepository.class);
        when(repository.findById(SysLogId.of(9L))).thenReturn(Optional.empty());

        SysLogQueryApplicationService service = new SysLogQueryApplicationService(repository);

        assertThrows(NotFoundException.class, () -> service.getById(SysLogId.of(9L)));
    }

    private SysLogRecord sysLog(Long id) {
        return SysLogRecord.reconstruct(
                id,
                "trace-" + id,
                "request-" + id,
                "UPMS",
                "QUERY_LOG",
                "QUERY",
                "SUCCESS",
                OperatorId.of("1001"),
                "alice",
                "127.0.0.1",
                "/api/upms/logs/page",
                "GET",
                12L,
                null,
                Instant.parse("2026-03-28T10:15:30Z"));
    }
}
