package com.github.thundax.bacon.storage.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储对象分页查询条件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectPageQueryDTO {

    /** 底层存储类型。 */
    private String storageType;
    /** 对象状态。 */
    private String objectStatus;
    /** 引用状态。 */
    private String referenceStatus;
    /** 原始文件名模糊匹配。 */
    private String originalFilename;
    /** 对象键模糊匹配。 */
    private String objectKey;
    /** 页码。 */
    private Integer pageNo;
    /** 每页条数。 */
    private Integer pageSize;
}
