package com.github.thundax.bacon.storage.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectPageRequest {

    @Pattern(regexp = "LOCAL_FILE|OSS", message = "storageType: must be LOCAL_FILE or OSS")
    private String storageType;

    @Pattern(regexp = "ACTIVE|DELETING|DELETED", message = "objectStatus: must be ACTIVE, DELETING or DELETED")
    private String objectStatus;

    @Pattern(regexp = "UNREFERENCED|REFERENCED", message = "referenceStatus: must be UNREFERENCED or REFERENCED")
    private String referenceStatus;

    private String originalFilename;

    private String objectKey;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
