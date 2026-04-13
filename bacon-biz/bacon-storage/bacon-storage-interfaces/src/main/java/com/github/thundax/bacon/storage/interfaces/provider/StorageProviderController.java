package com.github.thundax.bacon.storage.interfaces.provider;

import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageQueryDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/providers/storage")
@Tag(name = "Inner-Storage-Management", description = "Storage 域内部 Provider 接口")
public class StorageProviderController {

    private final StoredObjectFacade storedObjectFacade;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StorageProviderController(
            StoredObjectFacade storedObjectFacade,
            StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectFacade = storedObjectFacade;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "普通文件上传")
    @PostMapping(value = "/objects/upload", consumes = "multipart/form-data")
    public StoredObjectDTO uploadObject(
            @RequestParam("ownerType") String ownerType,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        UploadObjectCommand command = new UploadObjectCommand(
                ownerType,
                category,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream());
        return storedObjectFacade.uploadObject(command);
    }

    @Operation(summary = "初始化大文件分段上传")
    @PostMapping("/objects/multipart/init")
    public MultipartUploadSessionDTO initMultipartUpload(
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("originalFilename") String originalFilename,
            @RequestParam("contentType") String contentType,
            @RequestParam("totalSize") Long totalSize,
            @RequestParam("partSize") Long partSize) {
        return storedObjectFacade.initMultipartUpload(new InitMultipartUploadCommand(
                ownerType, ownerId, category, originalFilename, contentType, totalSize, partSize));
    }

    @Operation(summary = "上传大文件分段")
    @PostMapping(value = "/objects/multipart/{uploadId}/parts", consumes = "multipart/form-data")
    public MultipartUploadPartDTO uploadMultipartPart(
            @PathVariable("uploadId") String uploadId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId,
            @RequestParam("partNumber") Integer partNumber,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        return storedObjectFacade.uploadMultipartPart(new UploadMultipartPartCommand(
                uploadId, ownerType, ownerId, partNumber, file.getSize(), file.getInputStream()));
    }

    @Operation(summary = "完成大文件分段上传")
    @PostMapping("/objects/multipart/{uploadId}/complete")
    public StoredObjectDTO completeMultipartUpload(
            @PathVariable("uploadId") String uploadId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        return storedObjectFacade.completeMultipartUpload(new CompleteMultipartUploadCommand(uploadId, ownerType, ownerId));
    }

    @Operation(summary = "取消大文件分段上传")
    @DeleteMapping("/objects/multipart/{uploadId}")
    public void abortMultipartUpload(
            @PathVariable("uploadId") String uploadId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        storedObjectFacade.abortMultipartUpload(new AbortMultipartUploadCommand(uploadId, ownerType, ownerId));
    }

    @Operation(summary = "查询存储对象")
    @GetMapping("/objects/{objectId}")
    public StoredObjectDTO getObjectById(@PathVariable("objectId") String objectId) {
        return storedObjectQueryApplicationService.getObjectById(objectId);
    }

    @Operation(summary = "分页查询存储对象")
    @GetMapping("/objects")
    public StoredObjectPageResultDTO pageObjects(StoredObjectPageQueryDTO query) {
        return storedObjectQueryApplicationService.pageObjects(query);
    }

    @Operation(summary = "建立存储对象引用")
    @PostMapping("/objects/{objectId}/references")
    public void markObjectReferenced(
            @PathVariable("objectId") String objectId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        storedObjectFacade.markObjectReferenced(objectId, ownerType, ownerId);
    }

    @Operation(summary = "清理存储对象引用")
    @DeleteMapping("/objects/{objectId}/references")
    public void clearObjectReference(
            @PathVariable("objectId") String objectId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        storedObjectFacade.clearObjectReference(objectId, ownerType, ownerId);
    }

    @Operation(summary = "删除存储对象")
    @DeleteMapping("/objects/{objectId}")
    public void deleteObject(@PathVariable("objectId") String objectId) {
        storedObjectFacade.deleteObject(objectId);
    }
}
