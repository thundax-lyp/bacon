package com.github.thundax.bacon.upms.interfaces.controller;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.upms.application.command.DepartmentCommandApplicationService;
import com.github.thundax.bacon.upms.application.query.DepartmentQueryApplicationService;
import com.github.thundax.bacon.upms.interfaces.assembler.DepartmentInterfaceAssembler;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentBatchQueryRequest;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentSortUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.DepartmentUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentResponse;
import com.github.thundax.bacon.upms.interfaces.response.DepartmentTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@WrappedApiController
@RequestMapping("/upms/departments")
@Validated
@Tag(name = "UPMS-Department", description = "部门管理接口")
public class DepartmentController {

    private final DepartmentCommandApplicationService departmentCommandApplicationService;
    private final DepartmentQueryApplicationService departmentQueryApplicationService;

    public DepartmentController(
            DepartmentCommandApplicationService departmentCommandApplicationService,
            DepartmentQueryApplicationService departmentQueryApplicationService) {
        this.departmentCommandApplicationService = departmentCommandApplicationService;
        this.departmentQueryApplicationService = departmentQueryApplicationService;
    }

    @Operation(summary = "查询部门树")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "查询部门树", eventType = LogEventType.QUERY)
    @GetMapping("/tree")
    public List<DepartmentTreeResponse> getDepartmentTree() {
        return DepartmentInterfaceAssembler.toTreeResponseList(departmentQueryApplicationService.tree());
    }

    @Operation(summary = "创建部门")
    @HasPermission("sys:department:create")
    @SysLog(module = "UPMS", action = "创建部门", eventType = LogEventType.CREATE)
    @PostMapping
    public DepartmentResponse createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        return DepartmentInterfaceAssembler.toResponse(
                departmentCommandApplicationService.create(DepartmentInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "按部门 ID 查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "查询部门详情", eventType = LogEventType.QUERY)
    @GetMapping("/{departmentId}")
    public DepartmentResponse getDepartmentById(
            @PathVariable("departmentId") @Positive(message = "departmentId must be greater than 0") Long departmentId) {
        return DepartmentInterfaceAssembler.toResponse(
                departmentQueryApplicationService.getById(DepartmentId.of(departmentId)));
    }

    @Operation(summary = "按部门编码查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "按编码查询部门", eventType = LogEventType.QUERY)
    @GetMapping("/code/{departmentCode}")
    public DepartmentResponse getDepartmentByCode(
            @PathVariable("departmentCode") @NotBlank(message = "departmentCode must not be blank")
                    String departmentCode) {
        return DepartmentInterfaceAssembler.toResponse(
                departmentQueryApplicationService.getByCode(
                        DepartmentInterfaceAssembler.toDepartmentCode(departmentCode)));
    }

    @Operation(summary = "批量查询部门")
    @HasPermission("sys:department:view")
    @SysLog(module = "UPMS", action = "批量查询部门", eventType = LogEventType.QUERY)
    @GetMapping
    public List<DepartmentResponse> listByIds(@Valid @ModelAttribute DepartmentBatchQueryRequest request) {
        return DepartmentInterfaceAssembler.toResponseList(
                departmentQueryApplicationService.listByIds(DepartmentInterfaceAssembler.toDepartmentIds(request)));
    }

    @Operation(summary = "修改部门")
    @HasPermission("sys:department:update")
    @SysLog(module = "UPMS", action = "修改部门", eventType = LogEventType.UPDATE)
    @PutMapping("/{departmentId}")
    public DepartmentResponse updateDepartment(
            @PathVariable("departmentId") @Positive(message = "departmentId must be greater than 0") Long departmentId,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        return DepartmentInterfaceAssembler.toResponse(
                departmentCommandApplicationService.update(
                        DepartmentInterfaceAssembler.toUpdateCommand(departmentId, request)));
    }

    @Operation(summary = "调整部门排序")
    @HasPermission("sys:department:update")
    @SysLog(module = "UPMS", action = "调整部门排序", eventType = LogEventType.UPDATE)
    @PutMapping("/{departmentId}/sort")
    public DepartmentResponse updateSort(
            @PathVariable("departmentId") @Positive(message = "departmentId must be greater than 0") Long departmentId,
            @Valid @RequestBody DepartmentSortUpdateRequest request) {
        return DepartmentInterfaceAssembler.toResponse(
                departmentCommandApplicationService.updateSort(
                        DepartmentInterfaceAssembler.toSortUpdateCommand(departmentId, request)));
    }

    @Operation(summary = "删除部门")
    @HasPermission("sys:department:delete")
    @SysLog(module = "UPMS", action = "删除部门", eventType = LogEventType.DELETE)
    @DeleteMapping("/{departmentId}")
    public void delete(
            @PathVariable("departmentId") @Positive(message = "departmentId must be greater than 0") Long departmentId) {
        departmentCommandApplicationService.delete(DepartmentId.of(departmentId));
    }
}
