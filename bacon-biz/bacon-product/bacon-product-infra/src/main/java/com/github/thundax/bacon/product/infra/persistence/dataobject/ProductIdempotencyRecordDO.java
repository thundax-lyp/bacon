package com.github.thundax.bacon.product.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_product_idempotency_record")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class ProductIdempotencyRecordDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("operation_type")
    private String operationType;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("request_hash")
    private String requestHash;

    @TableField("result_ref_type")
    private String resultRefType;

    @TableField("result_ref_id")
    private String resultRefId;

    @TableField("result_payload")
    private String resultPayload;

    @TableField("idempotency_status")
    private String idempotencyStatus;

    @TableField("failure_reason")
    private String failureReason;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
