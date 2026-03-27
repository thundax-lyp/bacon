package com.github.thundax.bacon.storage.interfaces.provider;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/providers/storage")
@Tag(name = "Inner-Storage-Management", description = "Storage 域内部 Provider 接口")
public class StorageProviderController {

    private final StoredObjectApplicationService storedObjectApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StorageProviderController(StoredObjectApplicationService storedObjectApplicationService,
                                     StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectApplicationService = storedObjectApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "上传存储对象")
    @PostMapping(value = "/objects", consumes = "multipart/form-data")
    public StoredObjectDTO uploadObject(@RequestParam("ownerType") String ownerType,
                                        @RequestParam(value = "tenantId", required = false) String tenantId,
                                        @RequestParam(value = "category", required = false) String category,
                                        @RequestParam("file") MultipartFile file) throws IOException {
        UploadObjectCommand command = new UploadObjectCommand(ownerType, tenantId, category, file.getOriginalFilename(),
                file.getContentType(), file.getSize(), file.getInputStream());
        return storedObjectApplicationService.uploadObject(command);
    }

    @Operation(summary = "查询存储对象")
    @GetMapping("/objects/{objectId}")
    public StoredObjectDTO getObjectById(@PathVariable Long objectId) {
        return storedObjectQueryApplicationService.getObjectById(objectId);
    }

    @Operation(summary = "建立存储对象引用")
    @PostMapping("/objects/{objectId}/references")
    public void markObjectReferenced(@PathVariable Long objectId,
                                     @RequestParam("ownerType") String ownerType,
                                     @RequestParam("ownerId") String ownerId) {
        storedObjectApplicationService.markObjectReferenced(objectId, ownerType, ownerId);
    }

    @Operation(summary = "清理存储对象引用")
    @DeleteMapping("/objects/{objectId}/references")
    public void clearObjectReference(@PathVariable Long objectId,
                                     @RequestParam("ownerType") String ownerType,
                                     @RequestParam("ownerId") String ownerId) {
        storedObjectApplicationService.clearObjectReference(objectId, ownerType, ownerId);
    }

    @Operation(summary = "删除存储对象")
    @DeleteMapping("/objects/{objectId}")
    public void deleteObject(@PathVariable Long objectId) {
        storedObjectApplicationService.deleteObject(objectId);
    }
}
