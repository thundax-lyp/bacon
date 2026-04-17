package com.github.thundax.bacon.storage.api.request;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadObjectFacadeRequest {

    private String ownerType;
    private String category;
    private String originalFilename;
    private String contentType;
    private Long size;
    private InputStream inputStream;
}
