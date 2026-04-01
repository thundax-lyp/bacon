package com.github.thundax.bacon.boot;

import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class StorageRepositoryTestConfiguration {

    @Bean
    @Primary
    public StoredObjectRepository storedObjectRepository() {
        return Mockito.mock(StoredObjectRepository.class);
    }

    @Bean
    @Primary
    public StoredObjectReferenceRepository storedObjectReferenceRepository() {
        return Mockito.mock(StoredObjectReferenceRepository.class);
    }

    @Bean
    @Primary
    public StoredObjectStorageRepository storedObjectStorageRepository() {
        return Mockito.mock(StoredObjectStorageRepository.class);
    }

    @Bean
    @Primary
    public MultipartUploadSessionRepository multipartUploadSessionRepository() {
        return Mockito.mock(MultipartUploadSessionRepository.class);
    }

    @Bean
    @Primary
    public MultipartUploadPartRepository multipartUploadPartRepository() {
        return Mockito.mock(MultipartUploadPartRepository.class);
    }

    @Bean
    @Primary
    public StorageAuditLogRepository storageAuditLogRepository() {
        return Mockito.mock(StorageAuditLogRepository.class);
    }

    @Bean
    @Primary
    public StorageAuditOutboxRepository storageAuditOutboxRepository() {
        return Mockito.mock(StorageAuditOutboxRepository.class);
    }
}
