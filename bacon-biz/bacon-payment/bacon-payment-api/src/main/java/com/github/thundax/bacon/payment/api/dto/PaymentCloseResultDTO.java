package com.github.thundax.bacon.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付关闭结果传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCloseResultDTO {
    /** 支付单号。 */
    private String paymentNo;
    /** 关联订单号。 */
    private String orderNo;
    /** 支付状态。 */
    private String paymentStatus;
    /** 关闭结果。 */
    private String closeResult;
    /** 关闭原因。 */
    private String closeReason;
    /** 失败原因。 */
    private String failureReason;
}
