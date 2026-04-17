package com.github.thundax.bacon.storage.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分段上传分片传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadPartDTO {

    /** 分段上传会话业务键。 */
    private String uploadId;
    /** 分段序号。 */
    private Integer partNumber;
    /** 分段校验标识。 */
    private String etag;
}
