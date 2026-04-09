package com.github.thundax.bacon.storage.api.facade;

import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;

public interface StoredObjectFacade {

    StoredObjectDTO uploadObject(UploadObjectCommand command);

    MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command);

    MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command);

    StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command);

    void abortMultipartUpload(AbortMultipartUploadCommand command);

    StoredObjectDTO getObjectById(String objectId);

    void markObjectReferenced(String objectId, String ownerType, String ownerId);

    void clearObjectReference(String objectId, String ownerType, String ownerId);

    void deleteObject(String objectId);
}
