package com.github.thundax.bacon.storage.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitMultipartUploadFacadeRequest {

    private String ownerType;
    private String ownerId;
    private String category;
    private String originalFilename;
    private String contentType;
    private Long totalSize;
    private Long partSize;
}
