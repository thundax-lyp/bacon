package com.github.thundax.bacon.payment.interfaces.provider;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import com.github.thundax.bacon.payment.application.service.PaymentQueryService;
import org.springframework.stereotype.Component;

@Component
public class PaymentReadFacadeLocalImpl implements PaymentReadFacade {

    private final PaymentQueryService paymentQueryService;

    public PaymentReadFacadeLocalImpl(PaymentQueryService paymentQueryService) {
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
