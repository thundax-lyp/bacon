package com.github.thundax.bacon.storage.application.support;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.storage.application.config.StorageUploadLimitProperties;
import org.junit.jupiter.api.Test;

class StorageUploadLimitValidatorTest {

    @Test
    void shouldThrowBadRequestWhenSingleUploadExceedsLimit() {
        StorageUploadLimitProperties properties = new StorageUploadLimitProperties();
        properties.setSingleUploadMaxSizeBytes(1024L);
        StorageUploadLimitValidator validator = new StorageUploadLimitValidator(properties);

        assertThrows(BadRequestException.class, () -> validator.validateSingleUpload(2048L));
    }

    @Test
    void shouldThrowBadRequestWhenMultipartPartSizeIsInvalid() {
        StorageUploadLimitProperties properties = new StorageUploadLimitProperties();
        properties.setMultipartMaxTotalSizeBytes(4096L);
        properties.setMultipartPartSizeBytes(512L);
        StorageUploadLimitValidator validator = new StorageUploadLimitValidator(properties);

        assertThrows(BadRequestException.class, () -> validator.validateMultipartInit(2048L, 256L));
    }

    @Test
    void shouldThrowBadRequestWhenSingleUploadSizeIsNotPositive() {
        StorageUploadLimitProperties properties = new StorageUploadLimitProperties();
        StorageUploadLimitValidator validator = new StorageUploadLimitValidator(properties);

        assertThrows(BadRequestException.class, () -> validator.validateSingleUpload(0L));
    }
}
