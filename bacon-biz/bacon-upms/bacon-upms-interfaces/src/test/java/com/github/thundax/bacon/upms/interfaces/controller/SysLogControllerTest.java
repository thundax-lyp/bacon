package com.github.thundax.bacon.upms.interfaces.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.upms.application.audit.SysLogPageQuery;
import com.github.thundax.bacon.upms.application.audit.SysLogQueryApplicationService;
import com.github.thundax.bacon.upms.application.dto.SysLogDTO;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import com.github.thundax.bacon.upms.interfaces.request.SysLogPageRequest;
import com.github.thundax.bacon.upms.interfaces.response.SysLogPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.SysLogResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SysLogControllerTest {

    @Test
    void shouldDelegatePageWithAssembledQuery() {
        SysLogQueryApplicationService service = mock(SysLogQueryApplicationService.class);
        when(service.page(any())).thenReturn(new PageResult<>(List.of(sysLogDto(1L)), 1L, 1, 20));

        SysLogController controller = new SysLogController(service);

        SysLogPageResponse response = controller.page(new SysLogPageRequest("UPMS", null, "SUCCESS", "alice", 0, 500));

        ArgumentCaptor<SysLogPageQuery> queryCaptor = ArgumentCaptor.forClass(SysLogPageQuery.class);
        verify(service).page(queryCaptor.capture());
        SysLogPageQuery query = queryCaptor.getValue();
        assertEquals("UPMS", query.getModule());
        assertEquals(null, query.getEventType());
        assertEquals("SUCCESS", query.getResult());
        assertEquals("alice", query.getOperatorName());
        assertEquals(1, query.getPageNo());
        assertEquals(200, query.getPageSize());
        assertEquals(1, response.records().size());
    }

    @Test
    void shouldDelegateGetById() {
        SysLogQueryApplicationService service = mock(SysLogQueryApplicationService.class);
        when(service.getById(SysLogId.of(1L))).thenReturn(sysLogDto(1L));

        SysLogController controller = new SysLogController(service);

        SysLogResponse response = controller.getLogById(1L);

        verify(service).getById(SysLogId.of(1L));
        assertEquals(1L, response.id());
    }

    private SysLogDTO sysLogDto(Long id) {
        return new SysLogDTO(
                id,
                "trace-" + id,
                "request-" + id,
                "UPMS",
                "QUERY_LOG",
                "QUERY",
                "SUCCESS",
                "1001",
                "alice",
                "127.0.0.1",
                "/api/upms/logs/page",
                "GET",
                12L,
                null,
                Instant.parse("2026-03-28T10:15:30Z"));
    }
}
