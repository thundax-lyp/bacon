package com.github.thundax.bacon.payment.interfaces.provider;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.application.service.PaymentApplicationService;
import com.github.thundax.bacon.payment.application.service.PaymentCloseApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class PaymentCommandFacadeLocalImpl implements PaymentCommandFacade {

    private final PaymentApplicationService paymentApplicationService;
    private final PaymentCloseApplicationService paymentCloseApplicationService;

    public PaymentCommandFacadeLocalImpl(PaymentApplicationService paymentApplicationService,
                                         PaymentCloseApplicationService paymentCloseApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
        this.paymentCloseApplicationService = paymentCloseApplicationService;
    }

    @Override
    public PaymentCreateResultDTO createPayment(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                String channelCode, String subject, Instant expiredAt) {
        return paymentApplicationService.createPayment(tenantId, orderNo, userId, amount, channelCode, subject, expiredAt);
    }

    @Override
    public PaymentCloseResultDTO closePayment(Long tenantId, String paymentNo, String reason) {
        return paymentCloseApplicationService.closePayment(tenantId, paymentNo, reason);
    }
}
