package com.github.thundax.bacon.storage.interfaces.controller;

import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.application.command.MultipartUploadApplicationService;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.interfaces.dto.CompleteMultipartUploadRequest;
import com.github.thundax.bacon.storage.interfaces.dto.InitMultipartUploadRequest;
import com.github.thundax.bacon.storage.interfaces.dto.StoredObjectReferenceRequest;
import com.github.thundax.bacon.storage.interfaces.dto.UploadObjectRequest;
import com.github.thundax.bacon.storage.interfaces.dto.UploadMultipartPartRequest;
import com.github.thundax.bacon.storage.interfaces.response.MultipartUploadPartResponse;
import com.github.thundax.bacon.storage.interfaces.response.MultipartUploadSessionResponse;
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
    private final MultipartUploadApplicationService multipartUploadApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StoredObjectController(StoredObjectApplicationService storedObjectApplicationService,
                                  MultipartUploadApplicationService multipartUploadApplicationService,
                                  StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectApplicationService = storedObjectApplicationService;
        this.multipartUploadApplicationService = multipartUploadApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Operation(summary = "普通文件上传")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public StoredObjectResponse uploadObject(@ModelAttribute UploadObjectRequest request) throws IOException {
        UploadObjectCommand command = new UploadObjectCommand(request.ownerType(), request.tenantId(), request.category(),
                request.file().getOriginalFilename(), request.file().getContentType(), request.file().getSize(),
                request.file().getInputStream());
        StoredObjectDTO storedObject = storedObjectApplicationService.uploadObject(command);
        return StoredObjectResponse.from(storedObject);
    }

    @Operation(summary = "初始化大文件分段上传")
    @PostMapping("/multipart/init")
    public MultipartUploadSessionResponse initMultipartUpload(@ModelAttribute InitMultipartUploadRequest request) {
        InitMultipartUploadCommand command = new InitMultipartUploadCommand(request.ownerType(), request.tenantId(),
                request.category(), request.originalFilename(), request.contentType(), request.totalSize(),
                request.partSize());
        MultipartUploadSessionDTO session = multipartUploadApplicationService.initMultipartUpload(command);
        return MultipartUploadSessionResponse.from(session);
    }

    @Operation(summary = "上传大文件分段")
    @PostMapping(value = "/multipart/{uploadId}/parts", consumes = "multipart/form-data")
    public MultipartUploadPartResponse uploadMultipartPart(@PathVariable String uploadId,
                                                           @ModelAttribute UploadMultipartPartRequest request)
            throws IOException {
        UploadMultipartPartCommand command = new UploadMultipartPartCommand(uploadId, request.partNumber(),
                request.file().getSize(), request.file().getInputStream());
        MultipartUploadPartDTO part = multipartUploadApplicationService.uploadMultipartPart(command);
        return MultipartUploadPartResponse.from(part);
    }

    @Operation(summary = "完成大文件分段上传")
    @PostMapping("/multipart/{uploadId}/complete")
    public StoredObjectResponse completeMultipartUpload(@PathVariable String uploadId,
                                                        @ModelAttribute CompleteMultipartUploadRequest request) {
        CompleteMultipartUploadCommand command = new CompleteMultipartUploadCommand(uploadId, request.ownerId());
        return StoredObjectResponse.from(multipartUploadApplicationService.completeMultipartUpload(command));
    }

    @Operation(summary = "取消大文件分段上传")
    @DeleteMapping("/multipart/{uploadId}")
    public void abortMultipartUpload(@PathVariable String uploadId) {
        multipartUploadApplicationService.abortMultipartUpload(uploadId);
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
