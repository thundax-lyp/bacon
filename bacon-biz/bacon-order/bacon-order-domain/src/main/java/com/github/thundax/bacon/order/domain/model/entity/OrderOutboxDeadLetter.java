package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventId;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单出站事件死信记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutboxDeadLetter {

    /** 出站事件主键。 */
    private Long outboxId;
    /** 出站事件业务标识。 */
    private EventId eventId;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 事件类型。 */
    private OrderOutboxEventType eventType;
    /** 业务幂等键。 */
    private String businessKey;
    /** 事件载荷。 */
    private String payload;
    /** 重试次数。 */
    private Integer retryCount;
    /** 错误信息。 */
    private String errorMessage;
    /** 死信原因。 */
    private String deadReason;
    /** 死信时间。 */
    private Instant deadAt;
    /** 回放状态。 */
    private OrderOutboxReplayStatus replayStatus;
    /** 回放次数。 */
    private Integer replayCount;
    /** 最近一次回放时间。 */
    private Instant lastReplayAt;
    /** 最近一次回放结果信息。 */
    private String lastReplayMessage;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public String getEventIdValue() {
        return eventId == null ? null : eventId.value();
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public String getTenantIdRawValue() {
        return tenantId == null ? null : String.valueOf(tenantId.value());
    }

    public String getOrderNoValue() {
        return orderNo == null ? null : orderNo.value();
    }

    public String getEventTypeValue() {
        return eventType == null ? null : eventType.value();
    }

    public String getReplayStatusValue() {
        return replayStatus == null ? null : replayStatus.value();
    }
}
