package com.github.thundax.bacon.storage.interfaces.controller;

import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.interfaces.dto.StoredObjectReferenceRequest;
import com.github.thundax.bacon.storage.interfaces.dto.UploadObjectRequest;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@WrappedApiController
@RequestMapping("/storage/objects")
@Tag(name = "Storage-Object", description = "统一存储对象接口")
public class StoredObjectController {

    private final StoredObjectApplicationService storedObjectApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StoredObjectController(StoredObjectApplicationService storedObjectApplicationService,
                                  StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectApplicationService = storedObjectApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "上传存储对象")
    @PostMapping(consumes = "multipart/form-data")
    public StoredObjectResponse uploadObject(@ModelAttribute UploadObjectRequest request) throws IOException {
        UploadObjectCommand command = new UploadObjectCommand(request.ownerType(), request.tenantId(), request.category(),
                request.file().getOriginalFilename(), request.file().getContentType(), request.file().getSize(),
                request.file().getInputStream());
        StoredObjectDTO storedObject = storedObjectApplicationService.uploadObject(command);
        return StoredObjectResponse.from(storedObject);
    }

    @Operation(summary = "查询存储对象")
    @GetMapping("/{objectId}")
    public StoredObjectResponse getObjectById(@PathVariable Long objectId) {
        return StoredObjectResponse.from(storedObjectQueryApplicationService.getObjectById(objectId));
    }

    @Operation(summary = "建立存储对象引用")
    @PostMapping("/{objectId}/references")
    public void markObjectReferenced(@PathVariable Long objectId, @ModelAttribute StoredObjectReferenceRequest request) {
        storedObjectApplicationService.markObjectReferenced(objectId, request.ownerType(), request.ownerId());
    }

    @Operation(summary = "清理存储对象引用")
    @DeleteMapping("/{objectId}/references")
    public void clearObjectReference(@PathVariable Long objectId, @ModelAttribute StoredObjectReferenceRequest request) {
        storedObjectApplicationService.clearObjectReference(objectId, request.ownerType(), request.ownerId());
    }

    @Operation(summary = "删除存储对象")
    @DeleteMapping("/{objectId}")
    public void deleteObject(@PathVariable Long objectId) {
        storedObjectApplicationService.deleteObject(objectId);
    }
}
