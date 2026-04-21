package com.github.thundax.bacon.storage.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.storage.application.command.StoredObjectCommandApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.interfaces.assembler.StorageInterfaceAssembler;
import com.github.thundax.bacon.storage.interfaces.request.StoredObjectPageRequest;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectPageResponse;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    private final StoredObjectCommandApplicationService storedObjectCommandApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StorageController(
            StoredObjectCommandApplicationService storedObjectCommandApplicationService,
            StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectCommandApplicationService = storedObjectCommandApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "分页查询存储对象")
    @HasPermission("storage:object:view")
    @GetMapping
    public StoredObjectPageResponse page(@Valid @ModelAttribute StoredObjectPageRequest request) {
        return StoredObjectPageResponse.from(storedObjectQueryApplicationService.page(
                StorageInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "查询存储对象详情")
    @HasPermission("storage:object:view")
    @GetMapping("/{objectId}")
    public StoredObjectResponse getObjectById(
            @PathVariable("objectId") @NotBlank(message = "objectId must not be blank") String objectId) {
        return StoredObjectResponse.from(storedObjectQueryApplicationService.getObjectByNo(
                StorageInterfaceAssembler.toGetQuery(objectId)));
    }

    @Operation(summary = "删除存储对象")
    @HasPermission("storage:object:delete")
    @DeleteMapping("/{objectId}")
    public void deleteObject(
            @PathVariable("objectId") @NotBlank(message = "objectId must not be blank") String objectId) {
        storedObjectCommandApplicationService.deleteObject(StorageInterfaceAssembler.toDeleteCommand(objectId));
    }
}
