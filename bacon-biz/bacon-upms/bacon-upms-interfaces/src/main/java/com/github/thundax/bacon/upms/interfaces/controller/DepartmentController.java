package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.interfaces.dto.DepartmentBatchQueryRequest;
import com.github.thundax.bacon.upms.interfaces.dto.DepartmentCreateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.DepartmentSortUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.dto.DepartmentUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentResponse;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<DepartmentTreeResponse> getDepartmentTree() {
        return departmentApplicationService.getDepartmentTree().stream()
                .map(DepartmentTreeResponse::from)
                .toList();
    }

    @Operation(summary = "创建部门")
    @HasPermission("sys:department:create")
    @SysLog(module = "UPMS", action = "创建部门", eventType = LogEventType.CREATE)
    @PostMapping
    public DepartmentResponse createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        return DepartmentResponse.from(departmentApplicationService.createDepartment(
                normalize(request.code()),
                normalize(request.name()),
                DepartmentIdCodec.toDomain(request.parentId()),
                request.leaderUserId(),
                request.sort()));
    }

    @Operation(summary = "按部门 ID 查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "查询部门详情", eventType = LogEventType.QUERY)
    @GetMapping("/{departmentId}")
    public DepartmentResponse getDepartmentById(@PathVariable Long departmentId) {
        return DepartmentResponse.from(
                departmentApplicationService.getDepartmentById(DepartmentId.of(departmentId)));
    }

    @Operation(summary = "按部门编码查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "按编码查询部门", eventType = LogEventType.QUERY)
    @GetMapping("/code/{departmentCode}")
    public DepartmentResponse getDepartmentByCode(@PathVariable String departmentCode) {
        return DepartmentResponse.from(departmentApplicationService.getDepartmentByCode(normalize(departmentCode)));
    }

    @Operation(summary = "批量查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "批量查询部门", eventType = LogEventType.QUERY)
    @GetMapping
    public List<DepartmentResponse> listDepartmentsByIds(
            @ModelAttribute DepartmentBatchQueryRequest request) {
        Set<DepartmentId> departmentIds = request.getDepartmentIds() == null
                ? Set.of()
                : request.getDepartmentIds().stream()
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(Long::parseLong)
                        .map(DepartmentId::of)
                        .collect(Collectors.toSet());
        return departmentApplicationService.listDepartmentsByIds(departmentIds).stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Operation(summary = "修改部门")
    @HasPermission("sys:department:update")
    @SysLog(module = "UPMS", action = "修改部门", eventType = LogEventType.UPDATE)
    @PutMapping("/{departmentId}")
    public DepartmentResponse updateDepartment(
            @PathVariable Long departmentId,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        return DepartmentResponse.from(departmentApplicationService.updateDepartment(
                DepartmentId.of(departmentId),
                normalize(request.code()),
                normalize(request.name()),
                DepartmentIdCodec.toDomain(request.parentId()),
                request.leaderUserId(),
                request.sort()));
    }

    @Operation(summary = "调整部门排序")
    @HasPermission("sys:department:update")
    @SysLog(module = "UPMS", action = "调整部门排序", eventType = LogEventType.UPDATE)
    @PutMapping("/{departmentId}/sort")
    public DepartmentResponse updateSort(
            @PathVariable Long departmentId,
            @RequestBody DepartmentSortUpdateRequest request) {
        return DepartmentResponse.from(departmentApplicationService.updateDepartmentSort(
                DepartmentId.of(departmentId), request.sort()));
    }

    @Operation(summary = "删除部门")
    @HasPermission("sys:department:delete")
    @SysLog(module = "UPMS", action = "删除部门", eventType = LogEventType.DELETE)
    @DeleteMapping("/{departmentId}")
    public void deleteDepartment(@PathVariable Long departmentId) {
        departmentApplicationService.deleteDepartment(DepartmentId.of(departmentId));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
