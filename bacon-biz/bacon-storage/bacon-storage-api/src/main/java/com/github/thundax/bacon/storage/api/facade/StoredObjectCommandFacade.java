package com.github.thundax.bacon.storage.api.facade;

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

public interface StoredObjectCommandFacade {

    StoredObjectFacadeResponse uploadObject(UploadObjectFacadeRequest request);

    MultipartUploadSessionFacadeResponse initMultipartUpload(InitMultipartUploadFacadeRequest request);

    MultipartUploadPartFacadeResponse uploadMultipartPart(UploadMultipartPartFacadeRequest request);

    StoredObjectFacadeResponse completeMultipartUpload(CompleteMultipartUploadFacadeRequest request);

    void abortMultipartUpload(AbortMultipartUploadFacadeRequest request);

    void markObjectReferenced(StoredObjectReferenceFacadeRequest request);

    void clearObjectReference(StoredObjectReferenceFacadeRequest request);

    void deleteObject(StoredObjectDeleteFacadeRequest request);
}
