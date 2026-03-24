package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.service.DepartmentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/departments")
@Tag(name = "UPMS-Department", description = "部门查询接口")
public class DepartmentQueryController {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentQueryController(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @Operation(summary = "按部门 ID 查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "查询部门详情", eventType = LogEventType.QUERY)
    @GetMapping("/{departmentId}")
    public DepartmentDTO getDepartmentById(@RequestParam("tenantId") Long tenantId, @PathVariable Long departmentId) {
        return departmentApplicationService.getDepartmentById(tenantId, departmentId);
    }

    @Operation(summary = "按部门编码查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "按编码查询部门", eventType = LogEventType.QUERY)
    @GetMapping("/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(@RequestParam("tenantId") Long tenantId,
                                             @PathVariable String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(tenantId, departmentCode);
    }

    @Operation(summary = "批量查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "批量查询部门", eventType = LogEventType.QUERY)
    @GetMapping
    public java.util.List<DepartmentDTO> listDepartmentsByIds(@RequestParam("tenantId") Long tenantId,
                                                              @RequestParam("departmentIds") Set<Long> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(tenantId, departmentIds);
    }
}
