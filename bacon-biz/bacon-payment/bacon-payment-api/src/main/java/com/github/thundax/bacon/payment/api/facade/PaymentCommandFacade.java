package com.github.thundax.bacon.payment.api.facade;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentCommandFacade {

    PaymentCreateResultDTO createPayment(
            String orderNo, Long userId, BigDecimal amount, String channelCode, String subject, Instant expiredAt);

    PaymentCloseResultDTO closePayment(String paymentNo, String reason);
}
