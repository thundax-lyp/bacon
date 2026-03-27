package com.github.thundax.bacon.storage.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 分段上传会话持久化数据对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_storage_multipart_upload")
public class MultipartUploadSessionDO {

    /** 主键。 */
    private Long id;
    /** 分段上传会话业务键。 */
    @TableField("upload_id")
    private String uploadId;
    /** 所属租户业务键。 */
    @TableField("tenant_id")
    private String tenantId;
    /** 引用方类型。 */
    @TableField("owner_type")
    private String ownerType;
    /** 对象分类。 */
    @TableField("category")
    private String category;
    /** 原始文件名。 */
    @TableField("original_filename")
    private String originalFilename;
    /** 内容类型。 */
    @TableField("content_type")
    private String contentType;
    /** Storage 统一生成的对象键。 */
    @TableField("object_key")
    private String objectKey;
    /** 底层存储提供方分段上传会话标识。 */
    @TableField("provider_upload_id")
    private String providerUploadId;
    /** 总文件大小，字节。 */
    @TableField("total_size")
    private Long totalSize;
    /** 固定分段大小，字节。 */
    @TableField("part_size")
    private Long partSize;
    /** 已上传分段数。 */
    @TableField("uploaded_part_count")
    private Integer uploadedPartCount;
    /** 分段上传状态。 */
    @TableField("upload_status")
    private String uploadStatus;
    /** 创建时间。 */
    @TableField("created_at")
    private Instant createdAt;
    /** 更新时间。 */
    @TableField("updated_at")
    private Instant updatedAt;
    /** 完成时间。 */
    @TableField("completed_at")
    private Instant completedAt;
    /** 取消时间。 */
    @TableField("aborted_at")
    private Instant abortedAt;
}
