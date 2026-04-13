package com.github.thundax.bacon.storage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消分段上传命令。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbortMultipartUploadCommand {

    /** 分段上传会话业务键。 */
    private String uploadId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;
}
