package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.order.domain.model.valueobject.EventId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单出站事件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutboxEvent {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DEAD = "DEAD";

    public static final String EVENT_RESERVE_STOCK = "RESERVE_STOCK";
    public static final String EVENT_CREATE_PAYMENT = "CREATE_PAYMENT";
    public static final String EVENT_RELEASE_STOCK = "RELEASE_STOCK";

    /** 出站事件主键。 */
    private Long id;
    /** 出站事件业务标识。 */
    private EventId eventId;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 事件类型。 */
    private String eventType;
    /** 业务幂等键。 */
    private String businessKey;
    /** 事件载荷。 */
    private String payload;
    /** 当前状态。 */
    private String status;
    /** 重试次数。 */
    private Integer retryCount;
    /** 下次重试时间。 */
    private Instant nextRetryAt;
    /** 当前处理节点标识。 */
    private String processingOwner;
    /** 租约到期时间。 */
    private Instant leaseUntil;
    /** 领取处理时间。 */
    private Instant claimedAt;
    /** 错误信息。 */
    private String errorMessage;
    /** 死信原因。 */
    private String deadReason;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public String getEventIdValue() {
        return eventId == null ? null : eventId.value();
    }
}
