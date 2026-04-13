package com.github.thundax.bacon.storage.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储对象持久化数据对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_storage_object")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class StoredObjectDO {

    /** 主键。 */
    private Long id;
    /** 所属租户业务键。 */
    @TableField("tenant_id")
    private Long tenantId;
    /** 底层存储类型。 */
    @TableField("storage_type")
    private String storageType;
    /** 存储桶或本地逻辑目录。 */
    @TableField("bucket_name")
    private String bucketName;
    /** 底层对象键，全局唯一。 */
    @TableField("object_key")
    private String objectKey;
    /** 原始文件名。 */
    @TableField("original_filename")
    private String originalFilename;
    /** 内容类型。 */
    @TableField("content_type")
    private String contentType;
    /** 文件大小，字节。 */
    @TableField("size")
    private Long size;
    /** 由 Storage 派生的对象访问端点，仅用于展示/下载，不作为业务主数据持久化。 */
    @TableField("access_endpoint")
    private String accessEndpoint;
    /** 对象状态。 */
    @TableField("object_status")
    private String objectStatus;
    /** 引用状态。 */
    @TableField("reference_status")
    private String referenceStatus;
}
