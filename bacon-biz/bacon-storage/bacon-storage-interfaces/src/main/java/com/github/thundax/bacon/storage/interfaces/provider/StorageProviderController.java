package com.github.thundax.bacon.storage.interfaces.provider;

import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.request.AbortMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.CompleteMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.InitMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectDeleteFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadMultipartPartFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.api.response.MultipartUploadPartFacadeResponse;
import com.github.thundax.bacon.storage.api.response.MultipartUploadSessionFacadeResponse;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.interfaces.request.StoredObjectPageProviderRequest;
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

    private final StoredObjectCommandFacade storedObjectCommandFacade;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StorageProviderController(
            StoredObjectCommandFacade storedObjectCommandFacade,
            StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectCommandFacade = storedObjectCommandFacade;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "普通文件上传")
    @PostMapping(value = "/objects/upload", consumes = "multipart/form-data")
    public StoredObjectDTO uploadObject(
            @RequestParam("ownerType") String ownerType,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        UploadObjectFacadeRequest command = new UploadObjectFacadeRequest(
                ownerType,
                category,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream());
        return toStoredObjectDto(storedObjectCommandFacade.uploadObject(command));
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
        return toMultipartSessionDto(storedObjectCommandFacade.initMultipartUpload(new InitMultipartUploadFacadeRequest(
                ownerType, ownerId, category, originalFilename, contentType, totalSize, partSize)));
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
        return toMultipartPartDto(storedObjectCommandFacade.uploadMultipartPart(new UploadMultipartPartFacadeRequest(
                uploadId, ownerType, ownerId, partNumber, file.getSize(), file.getInputStream())));
    }

    @Operation(summary = "完成大文件分段上传")
    @PostMapping("/objects/multipart/{uploadId}/complete")
    public StoredObjectDTO completeMultipartUpload(
            @PathVariable("uploadId") String uploadId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        return toStoredObjectDto(storedObjectCommandFacade.completeMultipartUpload(
                new CompleteMultipartUploadFacadeRequest(uploadId, ownerType, ownerId)));
    }

    @Operation(summary = "取消大文件分段上传")
    @DeleteMapping("/objects/multipart/{uploadId}")
    public void abortMultipartUpload(
            @PathVariable("uploadId") String uploadId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        storedObjectCommandFacade.abortMultipartUpload(
                new AbortMultipartUploadFacadeRequest(uploadId, ownerType, ownerId));
    }

    @Operation(summary = "查询存储对象")
    @GetMapping("/objects/{objectId}")
    public StoredObjectDTO getObjectById(@PathVariable("objectId") String objectId) {
        return storedObjectQueryApplicationService.getObjectByNo(objectId);
    }

    @Operation(summary = "分页查询存储对象")
    @GetMapping("/objects")
    public StoredObjectPageResultDTO pageObjects(StoredObjectPageProviderRequest request) {
        return storedObjectQueryApplicationService.pageObjects(
                request.getStorageType() == null ? null : StorageType.from(request.getStorageType()),
                request.getObjectStatus() == null ? null : StoredObjectStatus.from(request.getObjectStatus()),
                request.getReferenceStatus() == null
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()),
                request.getOriginalFilename(),
                request.getObjectKey(),
                request.getPageNo(),
                request.getPageSize());
    }

    @Operation(summary = "建立存储对象引用")
    @PostMapping("/objects/{objectId}/references")
    public void markObjectReferenced(
            @PathVariable("objectId") String objectId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        storedObjectCommandFacade.markObjectReferenced(new StoredObjectReferenceFacadeRequest(objectId, ownerType, ownerId));
    }

    @Operation(summary = "清理存储对象引用")
    @DeleteMapping("/objects/{objectId}/references")
    public void clearObjectReference(
            @PathVariable("objectId") String objectId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") String ownerId) {
        storedObjectCommandFacade.clearObjectReference(new StoredObjectReferenceFacadeRequest(objectId, ownerType, ownerId));
    }

    @Operation(summary = "删除存储对象")
    @DeleteMapping("/objects/{objectId}")
    public void deleteObject(@PathVariable("objectId") String objectId) {
        storedObjectCommandFacade.deleteObject(new StoredObjectDeleteFacadeRequest(objectId));
    }

    private StoredObjectDTO toStoredObjectDto(StoredObjectFacadeResponse response) {
        if (response == null) {
            return null;
        }
        return new StoredObjectDTO(
                response.getStoredObjectNo(),
                response.getStorageType(),
                response.getBucketName(),
                response.getObjectKey(),
                response.getOriginalFilename(),
                response.getContentType(),
                response.getSize(),
                response.getAccessEndpoint(),
                response.getObjectStatus(),
                response.getReferenceStatus(),
                response.getCreatedAt());
    }

    private MultipartUploadSessionDTO toMultipartSessionDto(MultipartUploadSessionFacadeResponse response) {
        if (response == null) {
            return null;
        }
        return new MultipartUploadSessionDTO(
                response.getUploadId(),
                response.getOwnerType(),
                response.getOwnerId(),
                response.getCategory(),
                response.getOriginalFilename(),
                response.getContentType(),
                response.getTotalSize(),
                response.getPartSize(),
                response.getUploadedPartCount(),
                response.getUploadStatus());
    }

    private MultipartUploadPartDTO toMultipartPartDto(MultipartUploadPartFacadeResponse response) {
        if (response == null) {
            return null;
        }
        return new MultipartUploadPartDTO(response.getUploadId(), response.getPartNumber(), response.getEtag());
    }
}
