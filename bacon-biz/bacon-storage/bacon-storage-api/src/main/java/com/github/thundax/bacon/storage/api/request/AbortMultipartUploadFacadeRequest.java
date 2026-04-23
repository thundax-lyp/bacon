package com.github.thundax.bacon.storage.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AbortMultipartUploadFacadeRequest {

    private String uploadId;
    private String ownerType;
    private String ownerId;
}
