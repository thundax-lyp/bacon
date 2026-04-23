package com.github.thundax.bacon.storage.interfaces.provider;

import com.github.thundax.bacon.storage.application.command.StoredObjectCommandApplicationService;
import com.github.thundax.bacon.storage.api.request.AbortMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.CompleteMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.InitMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.interfaces.assembler.StorageInterfaceAssembler;
import com.github.thundax.bacon.storage.interfaces.request.StoredObjectPageProviderRequest;
import com.github.thundax.bacon.storage.interfaces.response.MultipartUploadPartResponse;
import com.github.thundax.bacon.storage.interfaces.response.MultipartUploadSessionResponse;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectPageResponse;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/providers/storage")
@Tag(name = "Inner-Storage-Management", description = "Storage 域内部 Provider 接口")
public class StorageProviderController {

    private final StoredObjectCommandApplicationService storedObjectCommandApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StorageProviderController(
            StoredObjectCommandApplicationService storedObjectCommandApplicationService,
            StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectCommandApplicationService = storedObjectCommandApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "普通文件上传")
    @PostMapping(value = "/commands/upload-object", consumes = "multipart/form-data")
    public StoredObjectResponse uploadObject(
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("file") @NotNull(message = "file must not be null") MultipartFile file)
            throws IOException {
        UploadObjectFacadeRequest command = new UploadObjectFacadeRequest(
                ownerType,
                category,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream());
        return StoredObjectResponse.from(storedObjectCommandApplicationService.uploadObject(
                StorageInterfaceAssembler.toUploadObjectCommand(command)));
    }

    @Operation(summary = "初始化大文件分段上传")
    @PostMapping("/commands/init-multipart-upload")
    public MultipartUploadSessionResponse initMultipartUpload(
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("originalFilename") @NotBlank(message = "originalFilename must not be blank")
                    String originalFilename,
            @RequestParam("contentType") @NotBlank(message = "contentType must not be blank") String contentType,
            @RequestParam("totalSize") @NotNull(message = "totalSize must not be null")
                    @Positive(message = "totalSize must be greater than 0")
                    Long totalSize,
            @RequestParam("partSize") @NotNull(message = "partSize must not be null")
                    @Positive(message = "partSize must be greater than 0")
                    Long partSize) {
        return MultipartUploadSessionResponse.from(storedObjectCommandApplicationService.initMultipartUpload(
                StorageInterfaceAssembler.toInitMultipartUploadCommand(new InitMultipartUploadFacadeRequest(
                        ownerType, ownerId, category, originalFilename, contentType, totalSize, partSize))));
    }

    @Operation(summary = "上传大文件分段")
    @PostMapping(value = "/commands/upload-multipart-part", consumes = "multipart/form-data")
    public MultipartUploadPartResponse uploadMultipartPart(
            @RequestParam("uploadId") @NotBlank(message = "uploadId must not be blank") String uploadId,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId,
            @RequestParam("partNumber") @NotNull(message = "partNumber must not be null")
                    @Positive(message = "partNumber must be greater than 0")
                    Integer partNumber,
            @RequestParam("file") @NotNull(message = "file must not be null") MultipartFile file)
            throws IOException {
        return MultipartUploadPartResponse.from(storedObjectCommandApplicationService.uploadMultipartPart(
                StorageInterfaceAssembler.toUploadMultipartPartCommand(
                        uploadId, ownerType, ownerId, partNumber, file)));
    }

    @Operation(summary = "完成大文件分段上传")
    @PostMapping("/commands/complete-multipart-upload")
    public StoredObjectResponse completeMultipartUpload(
            @RequestParam("uploadId") @NotBlank(message = "uploadId must not be blank") String uploadId,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        return StoredObjectResponse.from(storedObjectCommandApplicationService.completeMultipartUpload(
                StorageInterfaceAssembler.toCompleteMultipartUploadCommand(
                        new CompleteMultipartUploadFacadeRequest(uploadId, ownerType, ownerId))));
    }

    @Operation(summary = "取消大文件分段上传")
    @DeleteMapping("/commands/abort-multipart-upload")
    public void abortMultipartUpload(
            @RequestParam("uploadId") @NotBlank(message = "uploadId must not be blank") String uploadId,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        storedObjectCommandApplicationService.abortMultipartUpload(
                StorageInterfaceAssembler.toAbortMultipartUploadCommand(
                        new AbortMultipartUploadFacadeRequest(uploadId, ownerType, ownerId)));
    }

    @Operation(summary = "查询存储对象")
    @GetMapping("/queries/object")
    public StoredObjectResponse getObjectById(
            @RequestParam("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo) {
        return StoredObjectResponse.from(
                storedObjectQueryApplicationService.getObjectByNo(StorageInterfaceAssembler.toGetQuery(storedObjectNo)));
    }

    @Operation(summary = "分页查询存储对象")
    @GetMapping("/queries/page")
    public StoredObjectPageResponse page(@Valid @ModelAttribute StoredObjectPageProviderRequest request) {
        return StoredObjectPageResponse.from(
                storedObjectQueryApplicationService.page(StorageInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "建立存储对象引用")
    @PostMapping("/commands/mark-object-referenced")
    public void markObjectReferenced(
            @RequestParam("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        storedObjectCommandApplicationService.markObjectReferenced(
                StorageInterfaceAssembler.toReferenceCommand(new StoredObjectReferenceFacadeRequest(
                        storedObjectNo, ownerType, ownerId)));
    }

    @Operation(summary = "清理存储对象引用")
    @DeleteMapping("/commands/clear-object-reference")
    public void clearObjectReference(
            @RequestParam("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        storedObjectCommandApplicationService.clearObjectReference(
                StorageInterfaceAssembler.toReferenceCommand(new StoredObjectReferenceFacadeRequest(
                        storedObjectNo, ownerType, ownerId)));
    }

    @Operation(summary = "删除存储对象")
    @DeleteMapping("/commands/delete-object")
    public void deleteObject(
            @RequestParam("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo) {
        storedObjectCommandApplicationService.deleteObject(
                StorageInterfaceAssembler.toDeleteCommand(storedObjectNo));
    }

}
