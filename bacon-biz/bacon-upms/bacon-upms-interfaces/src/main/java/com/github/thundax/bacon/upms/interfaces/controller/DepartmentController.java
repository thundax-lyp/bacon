package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.service.DepartmentApplicationService;
import com.github.thundax.bacon.upms.interfaces.dto.DepartmentBatchQueryRequest;
import com.github.thundax.bacon.upms.interfaces.dto.DepartmentCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.DepartmentUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentResponse;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/upms/departments")
@Tag(name = "UPMS-Department", description = "部门管理接口")
public class DepartmentController {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentController(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @Operation(summary = "查询部门树")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "查询部门树", eventType = LogEventType.QUERY)
    @GetMapping("/tree")
    public List<DepartmentTreeResponse> getDepartmentTree(@RequestParam("tenantId") Long tenantId) {
        return departmentApplicationService.getDepartmentTree(tenantId).stream()
                .map(DepartmentTreeResponse::from)
                .toList();
    }

    @Operation(summary = "创建部门")
    @HasPermission("sys:department:create")
    @SysLog(module = "UPMS", action = "创建部门", eventType = LogEventType.CREATE)
    @PostMapping
    public DepartmentResponse createDepartment(@RequestBody DepartmentCreateRequest request) {
        return DepartmentResponse.from(departmentApplicationService.createDepartment(request.tenantId(), request.code(),
                request.name(), request.parentId(), request.leaderUserId()));
    }

    @Operation(summary = "按部门 ID 查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "查询部门详情", eventType = LogEventType.QUERY)
    @GetMapping("/{departmentId}")
    public DepartmentResponse getDepartmentById(@RequestParam("tenantId") Long tenantId, @PathVariable Long departmentId) {
        return DepartmentResponse.from(departmentApplicationService.getDepartmentById(tenantId, departmentId));
    }

    @Operation(summary = "按部门编码查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "按编码查询部门", eventType = LogEventType.QUERY)
    @GetMapping("/code/{departmentCode}")
    public DepartmentResponse getDepartmentByCode(@RequestParam("tenantId") Long tenantId,
                                                  @PathVariable String departmentCode) {
        return DepartmentResponse.from(departmentApplicationService.getDepartmentByCode(tenantId, departmentCode));
    }

    @Operation(summary = "批量查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "批量查询部门", eventType = LogEventType.QUERY)
    @GetMapping
    public List<DepartmentResponse> listDepartmentsByIds(@ModelAttribute DepartmentBatchQueryRequest request) {
        return departmentApplicationService.listDepartmentsByIds(request.getTenantId(), request.getDepartmentIds()).stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Operation(summary = "修改部门")
    @HasPermission("sys:department:update")
    @SysLog(module = "UPMS", action = "修改部门", eventType = LogEventType.UPDATE)
    @PutMapping("/{departmentId}")
    public DepartmentResponse updateDepartment(@PathVariable Long departmentId, @RequestBody DepartmentUpdateRequest request) {
        return DepartmentResponse.from(departmentApplicationService.updateDepartment(request.tenantId(), departmentId,
                request.code(), request.name(), request.parentId(), request.leaderUserId()));
    }

    @Operation(summary = "删除部门")
    @HasPermission("sys:department:delete")
    @SysLog(module = "UPMS", action = "删除部门", eventType = LogEventType.DELETE)
    @DeleteMapping("/{departmentId}")
    public void deleteDepartment(@RequestParam("tenantId") Long tenantId, @PathVariable Long departmentId) {
        departmentApplicationService.deleteDepartment(tenantId, departmentId);
    }
}
