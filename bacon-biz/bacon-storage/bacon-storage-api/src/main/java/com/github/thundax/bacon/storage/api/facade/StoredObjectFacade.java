package com.github.thundax.bacon.storage.api.facade;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;

public interface StoredObjectFacade {

    StoredObjectDTO uploadObject(UploadObjectCommand command);

    MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command);

    MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command);

    StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command);

    void abortMultipartUpload(AbortMultipartUploadCommand command);

    StoredObjectDTO getObjectById(Long objectId);

    void markObjectReferenced(Long objectId, String ownerType, String ownerId);

    void clearObjectReference(Long objectId, String ownerType, String ownerId);

    void deleteObject(Long objectId);
}
