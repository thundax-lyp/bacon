package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.service.DepartmentApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/upms/departments")
public class DepartmentQueryController {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentQueryController(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "查询部门详情", eventType = LogEventType.QUERY)
    @GetMapping("/{departmentId}")
    public DepartmentDTO getDepartmentById(@RequestParam("tenantId") Long tenantId, @PathVariable Long departmentId) {
        return departmentApplicationService.getDepartmentById(tenantId, departmentId);
    }

    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "按编码查询部门", eventType = LogEventType.QUERY)
    @GetMapping("/code/{departmentCode}")
    public DepartmentDTO getDepartmentByCode(@RequestParam("tenantId") Long tenantId,
                                             @PathVariable String departmentCode) {
        return departmentApplicationService.getDepartmentByCode(tenantId, departmentCode);
    }

    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "批量查询部门", eventType = LogEventType.QUERY)
    @GetMapping
    public java.util.List<DepartmentDTO> listDepartmentsByIds(@RequestParam("tenantId") Long tenantId,
                                                              @RequestParam("departmentIds") Set<Long> departmentIds) {
        return departmentApplicationService.listDepartmentsByIds(tenantId, departmentIds);
    }
}
