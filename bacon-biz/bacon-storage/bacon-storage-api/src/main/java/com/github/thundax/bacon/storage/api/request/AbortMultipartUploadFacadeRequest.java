package com.github.thundax.bacon.storage.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbortMultipartUploadFacadeRequest {

    private String uploadId;
    private String ownerType;
    private String ownerId;
}
