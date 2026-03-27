package com.github.thundax.bacon.common.oss.client;

import com.github.thundax.bacon.common.core.exception.SystemException;
import com.github.thundax.bacon.common.oss.config.CommonOssProperties;
import com.github.thundax.bacon.common.oss.model.ObjectStoragePart;
import com.github.thundax.bacon.common.oss.model.ObjectStorageWriteResult;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * 基于 S3 API 的对象存储客户端。
 */
public class S3ApiObjectStorageClient implements ObjectStorageClient {

    private final CommonOssProperties properties;
    private final S3Client s3Client;

    public S3ApiObjectStorageClient(CommonOssProperties properties) {
        this.properties = properties;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .region(Region.of(properties.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .build())
                .build();
    }

    @Override
    public ObjectStorageWriteResult putObject(String objectKey, String contentType, InputStream inputStream) {
        try {
            byte[] content = inputStream.readAllBytes();
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(properties.getBucketName())
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));
            return new ObjectStorageWriteResult(properties.getBucketName(), objectKey, buildAccessEndpoint(objectKey));
        } catch (IOException ex) {
            throw new SystemException("Failed to upload object through S3 API", ex);
        }
    }

    @Override
    public String initiateMultipartUpload(String objectKey, String contentType) {
        return s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                        .bucket(properties.getBucketName())
                        .key(objectKey)
                        .contentType(contentType)
                        .build())
                .uploadId();
    }

    @Override
    public String uploadPart(String objectKey, String uploadId, Integer partNumber, Long size, InputStream inputStream) {
        return s3Client.uploadPart(UploadPartRequest.builder()
                        .bucket(properties.getBucketName())
                        .key(objectKey)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .contentLength(size)
                        .build(),
                RequestBody.fromInputStream(inputStream, size))
                .eTag();
    }

    @Override
    public ObjectStorageWriteResult completeMultipartUpload(String objectKey, String uploadId,
                                                            List<ObjectStoragePart> parts) {
        List<CompletedPart> completedParts = parts.stream()
                .map(part -> CompletedPart.builder()
                        .partNumber(part.partNumber())
                        .eTag(part.etag())
                        .build())
                .toList();
        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();
        s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(properties.getBucketName())
                .key(objectKey)
                .uploadId(uploadId)
                .multipartUpload(multipartUpload)
                .build());
        return new ObjectStorageWriteResult(properties.getBucketName(), objectKey, buildAccessEndpoint(objectKey));
    }

    @Override
    public void abortMultipartUpload(String objectKey, String uploadId) {
        s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                .bucket(properties.getBucketName())
                .key(objectKey)
                .uploadId(uploadId)
                .build());
    }

    @Override
    public void deleteObject(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(objectKey)
                .build());
    }

    @Override
    public String buildAccessEndpoint(String objectKey) {
        String baseUrl = StringUtils.hasText(properties.getPublicBaseUrl())
                ? properties.getPublicBaseUrl() : properties.getEndpoint() + "/" + properties.getBucketName();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/" + objectKey;
    }
}
