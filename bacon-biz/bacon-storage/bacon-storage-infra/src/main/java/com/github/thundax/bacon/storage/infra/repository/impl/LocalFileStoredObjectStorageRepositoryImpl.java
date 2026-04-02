package com.github.thundax.bacon.storage.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.SystemException;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "bacon.storage.type", havingValue = "LOCAL_FILE", matchIfMissing = true)
public class LocalFileStoredObjectStorageRepositoryImpl implements StoredObjectStorageRepository {

    private final String rootPath;
    private final String bucketName;
    private final String publicBaseUrl;

    public LocalFileStoredObjectStorageRepositoryImpl(
            @Value("${bacon.storage.local.root-path:${java.io.tmpdir}/bacon/storage}") String rootPath,
            @Value("${bacon.storage.local.bucket-name:default}") String bucketName,
            @Value("${bacon.storage.local.public-base-url:/storage/files}") String publicBaseUrl) {
        this.rootPath = rootPath;
        this.bucketName = bucketName;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public StoredObjectStorageResult upload(String category, String originalFilename, String contentType,
                                            InputStream inputStream) {
        String objectKey = buildObjectKey(category, originalFilename);
        Path targetPath = resolveObjectPath(objectKey);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new StoredObjectStorageResult(StorageType.LOCAL_FILE, bucketName, objectKey,
                    buildAccessEndpoint(objectKey));
        } catch (IOException ex) {
            throw new SystemException("Failed to store file to local storage", ex);
        }
    }

    @Override
    public MultipartUploadStorageSession initMultipartUpload(String category, String originalFilename,
                                                             String contentType) {
        return new MultipartUploadStorageSession(buildObjectKey(category, originalFilename), null);
    }

    @Override
    public String uploadPart(MultipartUploadSession session, Integer partNumber, Long size, InputStream inputStream) {
        Path partPath = resolveMultipartPartPath(session.getUploadId(), partNumber);
        try {
            Files.createDirectories(partPath.getParent());
            Files.copy(inputStream, partPath, StandardCopyOption.REPLACE_EXISTING);
            return "PART-" + partNumber;
        } catch (IOException ex) {
            throw new SystemException("Failed to store multipart part to local storage", ex);
        }
    }

    @Override
    public StoredObjectStorageResult completeMultipartUpload(MultipartUploadSession session,
                                                             List<MultipartUploadPart> parts) {
        Path targetPath = resolveObjectPath(session.getObjectKey());
        try {
            Files.createDirectories(targetPath.getParent());
            try (OutputStream outputStream = Files.newOutputStream(targetPath, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (MultipartUploadPart part : parts.stream()
                        .sorted(Comparator.comparing(MultipartUploadPart::getPartNumber))
                        .toList()) {
                    Files.copy(resolveMultipartPartPath(session.getUploadId(), part.getPartNumber()), outputStream);
                }
            }
            deleteMultipartDirectory(session.getUploadId());
            return new StoredObjectStorageResult(StorageType.LOCAL_FILE, bucketName, session.getObjectKey(),
                    buildAccessEndpoint(session.getObjectKey()));
        } catch (IOException ex) {
            throw new SystemException("Failed to complete multipart upload in local storage", ex);
        }
    }

    @Override
    public void abortMultipartUpload(MultipartUploadSession session) {
        try {
            deleteMultipartDirectory(session.getUploadId());
        } catch (IOException ex) {
            throw new SystemException("Failed to abort multipart upload in local storage", ex);
        }
    }

    @Override
    public void delete(StoredObject storedObject) {
        try {
            Files.deleteIfExists(resolveObjectPath(storedObject.getObjectKey()));
        } catch (IOException ex) {
            throw new SystemException("Failed to delete file from local storage", ex);
        }
    }

    private String buildObjectKey(String category, String originalFilename) {
        String normalizedCategory = StringUtils.hasText(category) ? category.trim() : "default";
        String extension = extractExtension(originalFilename);
        return normalizedCategory + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index);
    }

    private Path resolveObjectPath(String objectKey) {
        return Path.of(rootPath, objectKey);
    }

    private Path resolveMultipartPartPath(String uploadId, Integer partNumber) {
        return Path.of(rootPath, "_multipart", uploadId, partNumber + ".part");
    }

    private String buildAccessEndpoint(String objectKey) {
        String normalizedBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        return normalizedBaseUrl + "/" + objectKey;
    }

    private void deleteMultipartDirectory(String uploadId) throws IOException {
        Path multipartDirectory = Path.of(rootPath, "_multipart", uploadId);
        if (!Files.exists(multipartDirectory)) {
            return;
        }
        try (var paths = Files.walk(multipartDirectory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new SystemException("Failed to clean multipart temporary files", ex);
                        }
                    });
        }
    }
}
