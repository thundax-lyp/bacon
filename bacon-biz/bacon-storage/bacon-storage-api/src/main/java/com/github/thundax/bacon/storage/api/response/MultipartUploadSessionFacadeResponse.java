package com.github.thundax.bacon.storage.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadSessionFacadeResponse {

    private String uploadId;
    private String ownerType;
    private String ownerId;
    private String category;
    private String originalFilename;
    private String contentType;
    private Long totalSize;
    private Long partSize;
    private Integer uploadedPartCount;
    private String uploadStatus;
}
