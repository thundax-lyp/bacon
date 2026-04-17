package com.github.thundax.bacon.storage.interfaces.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectPageProviderRequest {

    private String storageType;
    private String objectStatus;
    private String referenceStatus;
    private String originalFilename;
    private String objectKey;
    private Integer pageNo;
    private Integer pageSize;
}
