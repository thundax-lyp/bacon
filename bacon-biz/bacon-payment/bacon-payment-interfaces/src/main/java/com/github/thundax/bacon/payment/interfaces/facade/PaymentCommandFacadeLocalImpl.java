package com.github.thundax.bacon.payment.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
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
            String orderNo,
            Long userId,
            BigDecimal amount,
            String channelCode,
            String subject,
            Instant expiredAt) {
        Long tenantId = requireTenantId();
        return paymentCreateApplicationService.createPayment(
                tenantId, orderNo, userId, amount, channelCode, subject, expiredAt);
    }

    @Override
    public PaymentCloseResultDTO closePayment(String paymentNo, String reason) {
        Long tenantId = requireTenantId();
        return paymentCloseApplicationService.closePayment(tenantId, paymentNo, reason);
    }

    private Long requireTenantId() {
        Long tenantId = BaconContextHolder.currentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("tenantId must not be null");
        }
        return tenantId;
    }
}
