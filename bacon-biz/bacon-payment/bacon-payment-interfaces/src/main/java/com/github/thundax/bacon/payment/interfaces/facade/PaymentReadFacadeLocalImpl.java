package com.github.thundax.bacon.payment.interfaces.facade;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import com.github.thundax.bacon.payment.application.service.PaymentQueryApplicationService;
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
    public PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo) {
        return paymentQueryService.getByPaymentNo(tenantId, paymentNo);
    }

    @Override
    public PaymentDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return paymentQueryService.getByOrderNo(tenantId, orderNo);
    }
}
