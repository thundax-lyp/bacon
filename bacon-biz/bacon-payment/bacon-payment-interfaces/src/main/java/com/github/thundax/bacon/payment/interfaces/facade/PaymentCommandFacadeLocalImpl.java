package com.github.thundax.bacon.payment.interfaces.facade;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class PaymentCommandFacadeLocalImpl implements PaymentCommandFacade {

    private final PaymentCreateApplicationService paymentCreateApplicationService;
    private final PaymentCloseApplicationService paymentCloseApplicationService;

    public PaymentCommandFacadeLocalImpl(
            PaymentCreateApplicationService paymentCreateApplicationService,
            PaymentCloseApplicationService paymentCloseApplicationService) {
        this.paymentCreateApplicationService = paymentCreateApplicationService;
        this.paymentCloseApplicationService = paymentCloseApplicationService;
    }

    @Override
    public PaymentCreateResultDTO createPayment(
            Long tenantId,
            String orderNo,
            Long userId,
            BigDecimal amount,
            String channelCode,
            String subject,
            Instant expiredAt) {
        return paymentCreateApplicationService.createPayment(
                tenantId, orderNo, userId, amount, channelCode, subject, expiredAt);
    }

    @Override
    public PaymentCloseResultDTO closePayment(Long tenantId, String paymentNo, String reason) {
        return paymentCloseApplicationService.closePayment(tenantId, paymentNo, reason);
    }
}
