package com.github.thundax.bacon.storage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 完成分段上传命令。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMultipartUploadCommand {

    /** 分段上传会话业务键。 */
    private Long uploadId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;
    /** 所属租户业务键。 */
    private Long tenantId;
}
