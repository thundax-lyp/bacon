package com.github.thundax.bacon.storage.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadPartFacadeResponse {

    private String uploadId;
    private Integer partNumber;
    private String etag;
}
