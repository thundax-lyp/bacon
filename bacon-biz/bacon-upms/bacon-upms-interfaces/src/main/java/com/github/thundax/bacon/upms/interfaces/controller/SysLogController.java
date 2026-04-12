package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.api.dto.SysLogQueryDTO;
import com.github.thundax.bacon.upms.application.audit.SysLogQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.SysLogPageRequest;
import com.github.thundax.bacon.upms.interfaces.response.SysLogPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.SysLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/logs")
@Tag(name = "UPMS-SysLog", description = "系统访问日志查询接口")
public class SysLogController {

    private final SysLogQueryApplicationService sysLogQueryService;

    public SysLogController(SysLogQueryApplicationService sysLogQueryService) {
        this.sysLogQueryService = sysLogQueryService;
    }

    @Operation(summary = "分页查询系统访问日志")
    @HasPermission("sys:log:view")
    @SysLog(module = "UPMS", action = "分页查询系统日志", eventType = LogEventType.QUERY)
    @GetMapping("/page")
    public SysLogPageResponse pageLogs(@Valid @ModelAttribute SysLogPageRequest request) {
        return SysLogPageResponse.from(sysLogQueryService.pageLogs(new SysLogQueryDTO(
                request.getModule(),
                request.getEventType() == null ? null : request.getEventType().name(),
                request.getResult(),
                request.getOperatorName(),
                request.getPageNo(),
                request.getPageSize())));
    }

    @Operation(summary = "按日志 ID 查询系统访问日志")
    @HasPermission("sys:log:view")
    @SysLog(module = "UPMS", action = "查询系统日志详情", eventType = LogEventType.QUERY)
    @GetMapping("/{logId}")
    public SysLogResponse getLogById(@PathVariable Long logId) {
        return SysLogResponse.from(sysLogQueryService.getLogById(logId));
    }
}
