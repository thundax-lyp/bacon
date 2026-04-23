package com.github.thundax.bacon.storage.api.request;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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
