package com.github.thundax.bacon.storage.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_storage_object")
public class StoredObjectDO {

    private Long id;
    @TableField("tenant_id")
    private String tenantId;
    @TableField("storage_type")
    private String storageType;
    @TableField("bucket_name")
    private String bucketName;
    @TableField("object_key")
    private String objectKey;
    @TableField("original_filename")
    private String originalFilename;
    @TableField("content_type")
    private String contentType;
    @TableField("size")
    private Long size;
    @TableField("access_url")
    private String accessUrl;
    @TableField("object_status")
    private String objectStatus;
    @TableField("reference_status")
    private String referenceStatus;
    @TableField("created_by")
    private Long createdBy;
    @TableField("created_at")
    private Instant createdAt;
}
