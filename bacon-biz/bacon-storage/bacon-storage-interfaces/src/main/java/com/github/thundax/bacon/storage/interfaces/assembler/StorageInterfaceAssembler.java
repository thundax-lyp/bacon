package com.github.thundax.bacon.storage.interfaces.assembler;

import com.github.thundax.bacon.storage.api.request.AbortMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.CompleteMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.InitMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectDeleteFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectGetFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadMultipartPartFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.application.command.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.application.command.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.application.command.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.application.command.StoredObjectDeleteCommand;
import com.github.thundax.bacon.storage.application.command.StoredObjectReferenceCommand;
import com.github.thundax.bacon.storage.application.command.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.application.command.UploadObjectCommand;
import com.github.thundax.bacon.storage.application.query.StoredObjectGetQuery;
import com.github.thundax.bacon.storage.application.query.StoredObjectPageQuery;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
import com.github.thundax.bacon.storage.interfaces.request.StoredObjectPageProviderRequest;
import com.github.thundax.bacon.storage.interfaces.request.StoredObjectPageRequest;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public final class StorageInterfaceAssembler {

    private StorageInterfaceAssembler() {}

    public static UploadObjectCommand toUploadObjectCommand(UploadObjectFacadeRequest request) {
        return new UploadObjectCommand(
                request == null ? null : request.getOwnerType(),
                request == null ? null : request.getCategory(),
                request == null ? null : request.getOriginalFilename(),
                request == null ? null : request.getContentType(),
                request == null ? null : request.getSize(),
                request == null ? null : request.getInputStream());
    }

    public static UploadObjectCommand toUploadObjectCommand(
            String ownerType, String category, MultipartFile file) throws IOException {
        return new UploadObjectCommand(
                ownerType,
                category,
                file == null ? null : file.getOriginalFilename(),
                file == null ? null : file.getContentType(),
                file == null ? null : file.getSize(),
                file == null ? null : file.getInputStream());
    }

    public static InitMultipartUploadCommand toInitMultipartUploadCommand(InitMultipartUploadFacadeRequest request) {
        return new InitMultipartUploadCommand(
                request == null ? null : request.getOwnerType(),
                request == null ? null : request.getOwnerId(),
                request == null ? null : request.getCategory(),
                request == null ? null : request.getOriginalFilename(),
                request == null ? null : request.getContentType(),
                request == null ? null : request.getTotalSize(),
                request == null ? null : request.getPartSize());
    }

    public static UploadMultipartPartCommand toUploadMultipartPartCommand(UploadMultipartPartFacadeRequest request) {
        return new UploadMultipartPartCommand(
                request == null ? null : request.getUploadId(),
                request == null ? null : request.getOwnerType(),
                request == null ? null : request.getOwnerId(),
                request == null ? null : request.getPartNumber(),
                request == null ? null : request.getSize(),
                request == null ? null : request.getInputStream());
    }

    public static UploadMultipartPartCommand toUploadMultipartPartCommand(
            String uploadId, String ownerType, String ownerId, Integer partNumber, MultipartFile file)
            throws IOException {
        return new UploadMultipartPartCommand(
                uploadId,
                ownerType,
                ownerId,
                partNumber,
                file == null ? null : file.getSize(),
                file == null ? null : file.getInputStream());
    }

    public static CompleteMultipartUploadCommand toCompleteMultipartUploadCommand(
            CompleteMultipartUploadFacadeRequest request) {
        return new CompleteMultipartUploadCommand(
                request == null ? null : request.getUploadId(),
                request == null ? null : request.getOwnerType(),
                request == null ? null : request.getOwnerId());
    }

    public static AbortMultipartUploadCommand toAbortMultipartUploadCommand(AbortMultipartUploadFacadeRequest request) {
        return new AbortMultipartUploadCommand(
                request == null ? null : request.getUploadId(),
                request == null ? null : request.getOwnerType(),
                request == null ? null : request.getOwnerId());
    }

    public static StoredObjectGetQuery toGetQuery(StoredObjectGetFacadeRequest request) {
        return new StoredObjectGetQuery(request == null ? null : StoredObjectNo.of(request.getStoredObjectNo()));
    }

    public static StoredObjectGetQuery toGetQuery(String storedObjectNo) {
        return new StoredObjectGetQuery(StoredObjectNo.of(storedObjectNo));
    }

    public static StoredObjectPageQuery toPageQuery(StoredObjectPageRequest request) {
        return new StoredObjectPageQuery(
                request == null || request.getStorageType() == null ? null : StorageType.from(request.getStorageType()),
                request == null || request.getObjectStatus() == null ? null : StoredObjectStatus.from(request.getObjectStatus()),
                request == null || request.getReferenceStatus() == null
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()),
                request == null ? null : request.getOriginalFilename(),
                request == null ? null : request.getObjectKey(),
                request == null ? null : request.getPageNo(),
                request == null ? null : request.getPageSize());
    }

    public static StoredObjectPageQuery toPageQuery(StoredObjectPageProviderRequest request) {
        return new StoredObjectPageQuery(
                request == null || request.getStorageType() == null ? null : StorageType.from(request.getStorageType()),
                request == null || request.getObjectStatus() == null ? null : StoredObjectStatus.from(request.getObjectStatus()),
                request == null || request.getReferenceStatus() == null
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()),
                request == null ? null : request.getOriginalFilename(),
                request == null ? null : request.getObjectKey(),
                request == null ? null : request.getPageNo(),
                request == null ? null : request.getPageSize());
    }

    public static StoredObjectReferenceCommand toReferenceCommand(StoredObjectReferenceFacadeRequest request) {
        return new StoredObjectReferenceCommand(
                request == null ? null : StoredObjectNo.of(request.getStoredObjectNo()),
                request == null ? null : request.getOwnerType(),
                request == null ? null : request.getOwnerId());
    }

    public static StoredObjectDeleteCommand toDeleteCommand(StoredObjectDeleteFacadeRequest request) {
        return new StoredObjectDeleteCommand(
                request == null ? null : StoredObjectNo.of(request.getStoredObjectNo()));
    }

    public static StoredObjectDeleteCommand toDeleteCommand(String storedObjectNo) {
        return new StoredObjectDeleteCommand(StoredObjectNo.of(storedObjectNo));
    }
}
