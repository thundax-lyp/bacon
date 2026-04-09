package com.github.thundax.bacon.storage.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分段上传分片持久化数据对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_storage_multipart_upload_part")
public class MultipartUploadPartDO {

    /** 主键。 */
    private Long id;
    /** 分段上传会话业务键。 */
    @TableField("upload_id")
    private String uploadId;
    /** 分段序号。 */
    @TableField("part_number")
    private Integer partNumber;
    /** 分段校验标识。 */
    @TableField("etag")
    private String etag;
    /** 分段大小，字节。 */
    @TableField("size")
    private Long size;
    /** 创建时间。 */
    @TableField("created_at")
    private Instant createdAt;
}
