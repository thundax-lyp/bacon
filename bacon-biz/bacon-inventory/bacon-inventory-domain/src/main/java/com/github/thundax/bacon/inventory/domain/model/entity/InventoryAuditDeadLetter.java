package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计死信记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditDeadLetter {

    /** 死信记录主键。 */
    private DeadLetterId id;
    /** 审计出站主键。 */
    private OutboxId outboxId;
    /** 出站事件业务标识。 */
    private EventCode eventCode;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 操作类型。 */
    private InventoryAuditActionType actionType;
    /** 操作人类型。 */
    private InventoryAuditOperatorType operatorType;
    /** 操作人主键。 */
    private String operatorId;
    /** 原始发生时间。 */
    private Instant occurredAt;
    /** 重试次数。 */
    private Integer retryCount;
    /** 错误信息。 */
    private String errorMessage;
    /** 死信原因。 */
    private String deadReason;
    /** 死信时间。 */
    private Instant deadAt;
    /** 回放状态。 */
    private InventoryAuditReplayStatus replayStatus;
    /** 回放次数。 */
    private Integer replayCount;
    /** 最近一次回放时间。 */
    private Instant lastReplayAt;
    /** 最近一次回放结果。 */
    private String lastReplayResult;
    /** 最近一次回放错误。 */
    private String lastReplayError;
    /** 回放幂等键。 */
    private String replayKey;
    /** 回放操作人类型。 */
    private String replayOperatorType;
    /** 回放操作人主键。 */
    private String replayOperatorId;
}
