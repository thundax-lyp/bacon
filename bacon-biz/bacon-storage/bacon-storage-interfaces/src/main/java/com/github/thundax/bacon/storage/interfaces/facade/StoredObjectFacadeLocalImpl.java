package com.github.thundax.bacon.storage.interfaces.facade;

import com.github.thundax.bacon.storage.application.command.StoredObjectCommandApplicationService;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
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
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import com.github.thundax.bacon.storage.interfaces.assembler.StorageInterfaceAssembler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class StoredObjectFacadeLocalImpl implements StoredObjectCommandFacade, StoredObjectReadFacade {

    private final StoredObjectCommandApplicationService storedObjectCommandApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StoredObjectFacadeLocalImpl(
            StoredObjectCommandApplicationService storedObjectCommandApplicationService,
            StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectCommandApplicationService = storedObjectCommandApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Override
    public StoredObjectFacadeResponse uploadObject(UploadObjectFacadeRequest request) {
        StoredObjectDTO dto = storedObjectCommandApplicationService.uploadObject(
                StorageInterfaceAssembler.toUploadObjectCommand(request));
        return toStoredObjectFacadeResponse(dto);
    }

    @Override
    public MultipartUploadSessionFacadeResponse initMultipartUpload(InitMultipartUploadFacadeRequest request) {
        return toMultipartUploadSessionFacadeResponse(
                storedObjectCommandApplicationService.initMultipartUpload(
                        StorageInterfaceAssembler.toInitMultipartUploadCommand(request)));
    }

    @Override
    public MultipartUploadPartFacadeResponse uploadMultipartPart(UploadMultipartPartFacadeRequest request) {
        return toMultipartUploadPartFacadeResponse(
                storedObjectCommandApplicationService.uploadMultipartPart(
                        StorageInterfaceAssembler.toUploadMultipartPartCommand(request)));
    }

    @Override
    public StoredObjectFacadeResponse completeMultipartUpload(CompleteMultipartUploadFacadeRequest request) {
        return toStoredObjectFacadeResponse(storedObjectCommandApplicationService.completeMultipartUpload(
                StorageInterfaceAssembler.toCompleteMultipartUploadCommand(request)));
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadFacadeRequest request) {
        storedObjectCommandApplicationService.abortMultipartUpload(
                StorageInterfaceAssembler.toAbortMultipartUploadCommand(request));
    }

    @Override
    public StoredObjectFacadeResponse getObjectByNo(StoredObjectGetFacadeRequest request) {
        return toStoredObjectFacadeResponse(
                storedObjectQueryApplicationService.getObjectByNo(StorageInterfaceAssembler.toGetQuery(request)));
    }

    @Override
    public void markObjectReferenced(StoredObjectReferenceFacadeRequest request) {
        storedObjectCommandApplicationService.markObjectReferenced(StorageInterfaceAssembler.toReferenceCommand(request));
    }

    @Override
    public void clearObjectReference(StoredObjectReferenceFacadeRequest request) {
        storedObjectCommandApplicationService.clearObjectReference(StorageInterfaceAssembler.toReferenceCommand(request));
    }

    @Override
    public void deleteObject(StoredObjectDeleteFacadeRequest request) {
        storedObjectCommandApplicationService.deleteObject(StorageInterfaceAssembler.toDeleteCommand(request));
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
