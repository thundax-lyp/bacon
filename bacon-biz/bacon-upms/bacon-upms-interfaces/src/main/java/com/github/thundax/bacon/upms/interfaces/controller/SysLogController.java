package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.audit.SysLogQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.assembler.SysLogInterfaceAssembler;
import com.github.thundax.bacon.upms.domain.model.valueobject.SysLogId;
import com.github.thundax.bacon.upms.interfaces.request.SysLogPageRequest;
import com.github.thundax.bacon.upms.interfaces.response.SysLogPageResponse;
import com.github.thundax.bacon.upms.interfaces.response.SysLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@WrappedApiController
@RequestMapping("/upms/logs")
@Validated
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
    public SysLogPageResponse page(@Valid @ModelAttribute SysLogPageRequest request) {
        return SysLogPageResponse.from(sysLogQueryService.page(SysLogInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "按日志 ID 查询系统访问日志")
    @HasPermission("sys:log:view")
    @SysLog(module = "UPMS", action = "查询系统日志详情", eventType = LogEventType.QUERY)
    @GetMapping("/{logId}")
    public SysLogResponse getLogById(
            @PathVariable("logId") @Positive(message = "logId must be greater than 0") Long logId) {
        return SysLogResponse.from(sysLogQueryService.getById(SysLogId.of(logId)));
    }
}
