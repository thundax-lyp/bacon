package com.github.thundax.bacon.storage.interfaces.provider;

import com.github.thundax.bacon.storage.application.command.StoredObjectCommandApplicationService;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.request.AbortMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.CompleteMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.InitMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.interfaces.assembler.StorageInterfaceAssembler;
import com.github.thundax.bacon.storage.interfaces.request.StoredObjectPageProviderRequest;
import com.github.thundax.bacon.storage.interfaces.response.StoredObjectPageResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
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
    @PostMapping(value = "/objects/upload", consumes = "multipart/form-data")
    public StoredObjectDTO uploadObject(
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
        return storedObjectCommandApplicationService.uploadObject(
                StorageInterfaceAssembler.toUploadObjectCommand(command));
    }

    @Operation(summary = "初始化大文件分段上传")
    @PostMapping("/objects/multipart/init")
    public MultipartUploadSessionDTO initMultipartUpload(
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
        return storedObjectCommandApplicationService.initMultipartUpload(
                StorageInterfaceAssembler.toInitMultipartUploadCommand(new InitMultipartUploadFacadeRequest(
                        ownerType, ownerId, category, originalFilename, contentType, totalSize, partSize)));
    }

    @Operation(summary = "上传大文件分段")
    @PostMapping(value = "/objects/multipart/{uploadId}/parts", consumes = "multipart/form-data")
    public MultipartUploadPartDTO uploadMultipartPart(
            @PathVariable("uploadId") @NotBlank(message = "uploadId must not be blank") String uploadId,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId,
            @RequestParam("partNumber") @NotNull(message = "partNumber must not be null")
                    @Positive(message = "partNumber must be greater than 0")
                    Integer partNumber,
            @RequestParam("file") @NotNull(message = "file must not be null") MultipartFile file)
            throws IOException {
        return storedObjectCommandApplicationService.uploadMultipartPart(
                StorageInterfaceAssembler.toUploadMultipartPartCommand(
                        uploadId, ownerType, ownerId, partNumber, file));
    }

    @Operation(summary = "完成大文件分段上传")
    @PostMapping("/objects/multipart/{uploadId}/complete")
    public StoredObjectDTO completeMultipartUpload(
            @PathVariable("uploadId") @NotBlank(message = "uploadId must not be blank") String uploadId,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        return storedObjectCommandApplicationService.completeMultipartUpload(
                StorageInterfaceAssembler.toCompleteMultipartUploadCommand(
                        new CompleteMultipartUploadFacadeRequest(uploadId, ownerType, ownerId)));
    }

    @Operation(summary = "取消大文件分段上传")
    @DeleteMapping("/objects/multipart/{uploadId}")
    public void abortMultipartUpload(
            @PathVariable("uploadId") @NotBlank(message = "uploadId must not be blank") String uploadId,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        storedObjectCommandApplicationService.abortMultipartUpload(
                StorageInterfaceAssembler.toAbortMultipartUploadCommand(
                        new AbortMultipartUploadFacadeRequest(uploadId, ownerType, ownerId)));
    }

    @Operation(summary = "查询存储对象")
    @GetMapping("/objects/{storedObjectNo}")
    public StoredObjectDTO getObjectById(
            @PathVariable("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo) {
        return storedObjectQueryApplicationService.getObjectByNo(StorageInterfaceAssembler.toGetQuery(storedObjectNo));
    }

    @Operation(summary = "分页查询存储对象")
    @GetMapping("/objects")
    public StoredObjectPageResponse page(@Valid @ModelAttribute StoredObjectPageProviderRequest request) {
        return StoredObjectPageResponse.from(
                storedObjectQueryApplicationService.page(StorageInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "建立存储对象引用")
    @PostMapping("/objects/{storedObjectNo}/references")
    public void markObjectReferenced(
            @PathVariable("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        storedObjectCommandApplicationService.markObjectReferenced(
                StorageInterfaceAssembler.toReferenceCommand(new StoredObjectReferenceFacadeRequest(
                        storedObjectNo, ownerType, ownerId)));
    }

    @Operation(summary = "清理存储对象引用")
    @DeleteMapping("/objects/{storedObjectNo}/references")
    public void clearObjectReference(
            @PathVariable("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo,
            @RequestParam("ownerType") @NotBlank(message = "ownerType must not be blank") String ownerType,
            @RequestParam("ownerId") @NotBlank(message = "ownerId must not be blank") String ownerId) {
        storedObjectCommandApplicationService.clearObjectReference(
                StorageInterfaceAssembler.toReferenceCommand(new StoredObjectReferenceFacadeRequest(
                        storedObjectNo, ownerType, ownerId)));
    }

    @Operation(summary = "删除存储对象")
    @DeleteMapping("/objects/{storedObjectNo}")
    public void deleteObject(
            @PathVariable("storedObjectNo") @NotBlank(message = "storedObjectNo must not be blank")
                    String storedObjectNo) {
        storedObjectCommandApplicationService.deleteObject(
                StorageInterfaceAssembler.toDeleteCommand(storedObjectNo));
    }

}
