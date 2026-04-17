package com.github.thundax.bacon.storage.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.interfaces.request.StoredObjectPageRequest;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectPageResponse;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/storage/objects")
@Tag(name = "Storage-Management", description = "存储对象管理接口")
public class StorageController {

    private final StoredObjectApplicationService storedObjectApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StorageController(
            StoredObjectApplicationService storedObjectApplicationService,
            StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectApplicationService = storedObjectApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "分页查询存储对象")
    @HasPermission("storage:object:view")
    @GetMapping
    public StoredObjectPageResponse pageObjects(@Valid @ModelAttribute StoredObjectPageRequest request) {
        return StoredObjectPageResponse.from(storedObjectQueryApplicationService.pageObjects(
                request.getStorageType() == null ? null : StorageType.from(request.getStorageType()),
                request.getObjectStatus() == null ? null : StoredObjectStatus.from(request.getObjectStatus()),
                request.getReferenceStatus() == null
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()),
                request.getOriginalFilename(),
                request.getObjectKey(),
                request.getPageNo(),
                request.getPageSize()));
    }

    @Operation(summary = "查询存储对象详情")
    @HasPermission("storage:object:view")
    @GetMapping("/{objectId}")
    public StoredObjectResponse getObjectById(@PathVariable("objectId") String objectId) {
        return StoredObjectResponse.from(storedObjectQueryApplicationService.getObjectByNo(objectId));
    }

    @Operation(summary = "删除存储对象")
    @HasPermission("storage:object:delete")
    @DeleteMapping("/{objectId}")
    public void deleteObject(@PathVariable("objectId") String objectId) {
        storedObjectApplicationService.deleteObject(objectId);
    }
}
