package com.github.thundax.bacon.storage.interfaces.dto;

import com.github.thundax.bacon.storage.api.enums.ObjectStatusEnum;
import com.github.thundax.bacon.storage.api.enums.ReferenceStatusEnum;
import com.github.thundax.bacon.storage.api.enums.StorageTypeEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectPageRequest {

    private String tenantCode;

    private StorageTypeEnum storageType;

    private ObjectStatusEnum objectStatus;

    private ReferenceStatusEnum referenceStatus;

    private String originalFilename;

    private String objectKey;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
