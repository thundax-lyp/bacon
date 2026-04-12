package com.github.thundax.bacon.payment.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class PaymentReadFacadeLocalImpl implements PaymentReadFacade {

    private final PaymentQueryApplicationService paymentQueryService;

    public PaymentReadFacadeLocalImpl(PaymentQueryApplicationService paymentQueryService) {
        this.paymentQueryService = paymentQueryService;
    }

    @Override
    public PaymentDetailDTO getByPaymentNo(String paymentNo) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return paymentQueryService.getByPaymentNo(tenantId, paymentNo);
    }

    @Override
    public PaymentDetailDTO getByOrderNo(String orderNo) {
        Long tenantId = BaconContextHolder.requireTenantId();
        return paymentQueryService.getByOrderNo(tenantId, orderNo);
    }
}
