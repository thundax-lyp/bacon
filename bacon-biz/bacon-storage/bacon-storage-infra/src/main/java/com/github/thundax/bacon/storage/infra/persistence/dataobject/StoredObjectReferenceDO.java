package com.github.thundax.bacon.storage.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储对象引用关系持久化数据对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_storage_object_reference")
public class StoredObjectReferenceDO {

    /** 存储对象主键。 */
    @TableField("object_id")
    private StoredObjectId objectId;
    /** 引用方类型。 */
    @TableField("owner_type")
    private String ownerType;
    /** 引用方业务主键。 */
    @TableField("owner_id")
    private String ownerId;
}
