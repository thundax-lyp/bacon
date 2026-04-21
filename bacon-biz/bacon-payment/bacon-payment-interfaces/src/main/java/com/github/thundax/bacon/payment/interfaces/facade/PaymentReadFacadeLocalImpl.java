package com.github.thundax.bacon.payment.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import com.github.thundax.bacon.payment.interfaces.assembler.PaymentInterfaceAssembler;
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
    public PaymentDetailFacadeResponse getByPaymentNo(PaymentGetByPaymentNoFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return PaymentInterfaceAssembler.toDetailFacadeResponse(
                paymentQueryService.getByPaymentNo(PaymentInterfaceAssembler.toGetByPaymentNoQuery(request)));
    }

    @Override
    public PaymentDetailFacadeResponse getByOrderNo(PaymentGetByOrderNoFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return PaymentInterfaceAssembler.toDetailFacadeResponse(
                paymentQueryService.getByOrderNo(PaymentInterfaceAssembler.toGetByOrderNoQuery(request)));
    }
}
