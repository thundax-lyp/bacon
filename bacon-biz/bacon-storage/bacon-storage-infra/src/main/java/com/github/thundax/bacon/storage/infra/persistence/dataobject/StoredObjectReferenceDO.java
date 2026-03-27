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
@TableName("bacon_storage_object_reference")
public class StoredObjectReferenceDO {

    private Long id;
    @TableField("object_id")
    private Long objectId;
    @TableField("owner_type")
    private String ownerType;
    @TableField("owner_id")
    private String ownerId;
    @TableField("created_at")
    private Instant createdAt;
}
