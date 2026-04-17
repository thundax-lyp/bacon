package com.github.thundax.bacon.storage.interfaces.facade;

import com.github.thundax.bacon.storage.application.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.application.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.application.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.application.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.storage.api.request.AbortMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.CompleteMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.InitMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectDeleteFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectGetFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadMultipartPartFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.api.response.MultipartUploadPartFacadeResponse;
import com.github.thundax.bacon.storage.api.response.MultipartUploadSessionFacadeResponse;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.storage.application.command.MultipartUploadApplicationService;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class StoredObjectFacadeLocalImpl implements StoredObjectCommandFacade, StoredObjectReadFacade {

    private final StoredObjectApplicationService storedObjectApplicationService;
    private final MultipartUploadApplicationService multipartUploadApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StoredObjectFacadeLocalImpl(
            StoredObjectApplicationService storedObjectApplicationService,
            MultipartUploadApplicationService multipartUploadApplicationService,
            StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectApplicationService = storedObjectApplicationService;
        this.multipartUploadApplicationService = multipartUploadApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Override
    public StoredObjectFacadeResponse uploadObject(UploadObjectFacadeRequest request) {
        StoredObjectDTO dto = storedObjectApplicationService.uploadObject(new UploadObjectCommand(
                request.getOwnerType(),
                request.getCategory(),
                request.getOriginalFilename(),
                request.getContentType(),
                request.getSize(),
                request.getInputStream()));
        return toStoredObjectFacadeResponse(dto);
    }

    @Override
    public MultipartUploadSessionFacadeResponse initMultipartUpload(InitMultipartUploadFacadeRequest request) {
        return toMultipartUploadSessionFacadeResponse(
                multipartUploadApplicationService.initMultipartUpload(new InitMultipartUploadCommand(
                        request.getOwnerType(),
                        request.getOwnerId(),
                        request.getCategory(),
                        request.getOriginalFilename(),
                        request.getContentType(),
                        request.getTotalSize(),
                        request.getPartSize())));
    }

    @Override
    public MultipartUploadPartFacadeResponse uploadMultipartPart(UploadMultipartPartFacadeRequest request) {
        return toMultipartUploadPartFacadeResponse(
                multipartUploadApplicationService.uploadMultipartPart(new UploadMultipartPartCommand(
                        request.getUploadId(),
                        request.getOwnerType(),
                        request.getOwnerId(),
                        request.getPartNumber(),
                        request.getSize(),
                        request.getInputStream())));
    }

    @Override
    public StoredObjectFacadeResponse completeMultipartUpload(CompleteMultipartUploadFacadeRequest request) {
        return toStoredObjectFacadeResponse(multipartUploadApplicationService.completeMultipartUpload(
                new CompleteMultipartUploadCommand(request.getUploadId(), request.getOwnerType(), request.getOwnerId())));
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadFacadeRequest request) {
        multipartUploadApplicationService.abortMultipartUpload(
                new AbortMultipartUploadCommand(request.getUploadId(), request.getOwnerType(), request.getOwnerId()));
    }

    @Override
    public StoredObjectFacadeResponse getObjectByNo(StoredObjectGetFacadeRequest request) {
        return toStoredObjectFacadeResponse(storedObjectQueryApplicationService.getObjectByNo(request.getStoredObjectNo()));
    }

    @Override
    public void markObjectReferenced(StoredObjectReferenceFacadeRequest request) {
        storedObjectApplicationService.markObjectReferenced(
                request.getStoredObjectNo(), request.getOwnerType(), request.getOwnerId());
    }

    @Override
    public void clearObjectReference(StoredObjectReferenceFacadeRequest request) {
        storedObjectApplicationService.clearObjectReference(
                request.getStoredObjectNo(), request.getOwnerType(), request.getOwnerId());
    }

    @Override
    public void deleteObject(StoredObjectDeleteFacadeRequest request) {
        storedObjectApplicationService.deleteObject(request.getStoredObjectNo());
    }

    private StoredObjectFacadeResponse toStoredObjectFacadeResponse(StoredObjectDTO dto) {
        if (dto == null) {
            return null;
        }
        return new StoredObjectFacadeResponse(
                dto.getStoredObjectNo(),
                dto.getStorageType(),
                dto.getBucketName(),
                dto.getObjectKey(),
                dto.getOriginalFilename(),
                dto.getContentType(),
                dto.getSize(),
                dto.getAccessEndpoint(),
                dto.getObjectStatus(),
                dto.getReferenceStatus(),
                dto.getCreatedAt());
    }

    private MultipartUploadSessionFacadeResponse toMultipartUploadSessionFacadeResponse(
            MultipartUploadSessionDTO dto) {
        if (dto == null) {
            return null;
        }
        return new MultipartUploadSessionFacadeResponse(
                dto.getUploadId(),
                dto.getOwnerType(),
                dto.getOwnerId(),
                dto.getCategory(),
                dto.getOriginalFilename(),
                dto.getContentType(),
                dto.getTotalSize(),
                dto.getPartSize(),
                dto.getUploadedPartCount(),
                dto.getUploadStatus());
    }

    private MultipartUploadPartFacadeResponse toMultipartUploadPartFacadeResponse(
            MultipartUploadPartDTO dto) {
        if (dto == null) {
            return null;
        }
        return new MultipartUploadPartFacadeResponse(dto.getUploadId(), dto.getPartNumber(), dto.getEtag());
    }
}
