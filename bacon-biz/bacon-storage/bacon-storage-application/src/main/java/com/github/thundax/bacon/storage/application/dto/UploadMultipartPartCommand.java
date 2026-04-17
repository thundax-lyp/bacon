package com.github.thundax.bacon.storage.application.dto;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传分段命令。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadMultipartPartCommand {

    /** 分段上传会话业务键。 */
    private String uploadId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;
    /** 分段序号。 */
    private Integer partNumber;
    /** 分段大小，字节。 */
    private Long size;
    /** 上传输入流。 */
    private InputStream inputStream;
}
