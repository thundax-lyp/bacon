package com.github.thundax.bacon.storage.interfaces.facade;

import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
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
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        return storedObjectApplicationService.uploadObject(command);
    }

    @Override
    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        return multipartUploadApplicationService.initMultipartUpload(command);
    }

    @Override
    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        return multipartUploadApplicationService.uploadMultipartPart(command);
    }

    @Override
    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        return multipartUploadApplicationService.completeMultipartUpload(command);
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadCommand command) {
        multipartUploadApplicationService.abortMultipartUpload(command);
    }

    @Override
    public StoredObjectDTO getObjectByNo(String storedObjectNo) {
        return storedObjectQueryApplicationService.getObjectByNo(storedObjectNo);
    }

    @Override
    public void markObjectReferenced(String storedObjectNo, String ownerType, String ownerId) {
        storedObjectApplicationService.markObjectReferenced(storedObjectNo, ownerType, ownerId);
    }

    @Override
    public void clearObjectReference(String storedObjectNo, String ownerType, String ownerId) {
        storedObjectApplicationService.clearObjectReference(storedObjectNo, ownerType, ownerId);
    }

    @Override
    public void deleteObject(String storedObjectNo) {
        storedObjectApplicationService.deleteObject(storedObjectNo);
    }
}
