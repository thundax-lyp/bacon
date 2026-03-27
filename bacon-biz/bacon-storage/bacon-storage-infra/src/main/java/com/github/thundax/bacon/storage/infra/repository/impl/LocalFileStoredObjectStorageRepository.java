package com.github.thundax.bacon.storage.infra.repository.impl;

import com.github.thundax.bacon.common.core.exception.SystemException;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.enums.StorageTypeEnum;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "bacon.storage.type", havingValue = "LOCAL_FILE", matchIfMissing = true)
public class LocalFileStoredObjectStorageRepository implements StoredObjectStorageRepository {

    private final String rootPath;
    private final String bucketName;
    private final String publicBaseUrl;

    public LocalFileStoredObjectStorageRepository(
            @Value("${bacon.storage.local.root-path:${java.io.tmpdir}/bacon/storage}") String rootPath,
            @Value("${bacon.storage.local.bucket-name:default}") String bucketName,
            @Value("${bacon.storage.local.public-base-url:/storage/files}") String publicBaseUrl) {
        this.rootPath = rootPath;
        this.bucketName = bucketName;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public StoredObjectStorageResult upload(UploadObjectCommand command) {
        String objectKey = buildObjectKey(command.getCategory(), command.getOriginalFilename());
        Path targetPath = Path.of(rootPath, objectKey);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(command.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new StoredObjectStorageResult(StorageTypeEnum.LOCAL_FILE.name(), bucketName, objectKey,
                    buildAccessUrl(objectKey));
        } catch (IOException ex) {
            throw new SystemException("Failed to store file to local storage", ex);
        }
    }

    @Override
    public void delete(StoredObject storedObject) {
        try {
            Files.deleteIfExists(Path.of(rootPath, storedObject.getObjectKey()));
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

    private String buildAccessUrl(String objectKey) {
        String normalizedBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        return normalizedBaseUrl + "/" + objectKey;
    }
}
