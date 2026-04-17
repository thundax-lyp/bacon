package com.github.thundax.bacon.storage.api.request;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadMultipartPartFacadeRequest {

    private String uploadId;
    private String ownerType;
    private String ownerId;
    private Integer partNumber;
    private Long size;
    private InputStream inputStream;
}
