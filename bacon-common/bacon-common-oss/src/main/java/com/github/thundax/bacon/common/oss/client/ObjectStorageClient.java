package com.github.thundax.bacon.common.oss.client;

import com.github.thundax.bacon.common.oss.model.ObjectStoragePart;
import com.github.thundax.bacon.common.oss.model.ObjectStorageWriteResult;

import java.io.InputStream;
import java.util.List;

/**
 * 通用对象存储客户端。
 */
public interface ObjectStorageClient {

    ObjectStorageWriteResult putObject(String objectKey, String contentType, InputStream inputStream);

    String initiateMultipartUpload(String objectKey, String contentType);

    String uploadPart(String objectKey, String uploadId, Integer partNumber, Long size, InputStream inputStream);

    ObjectStorageWriteResult completeMultipartUpload(String objectKey, String uploadId, List<ObjectStoragePart> parts);

    void abortMultipartUpload(String objectKey, String uploadId);

    void deleteObject(String objectKey);

    String buildAccessEndpoint(String objectKey);
}
