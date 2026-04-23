package com.github.thundax.bacon.storage.api.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectFacadeResponse {

    private String storedObjectNo;
    private String storageType;
    private String bucketName;
    private String objectKey;
    private String originalFilename;
    private String contentType;
    private Long size;
    private String accessEndpoint;
    private String objectStatus;
    private String referenceStatus;
    private Instant createdAt;
}
