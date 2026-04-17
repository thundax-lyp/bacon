package com.github.thundax.bacon.payment.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.payment.application.command.PaymentCloseResult;
import com.github.thundax.bacon.payment.application.command.PaymentCreateResult;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.interfaces.assembler.PaymentFacadeResponseAssembler;
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
    public PaymentCreateFacadeResponse createPayment(PaymentCreateFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        PaymentCreateResult result = paymentCreateApplicationService.createPayment(
                request.getOrderNo(),
                request.getUserId(),
                request.getAmount(),
                request.getChannelCode(),
                request.getSubject(),
                request.getExpiredAt());
        return PaymentFacadeResponseAssembler.fromCreateResult(result);
    }

    @Override
    public PaymentCloseFacadeResponse closePayment(PaymentCloseFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        PaymentCloseResult result =
                paymentCloseApplicationService.closePayment(request.getPaymentNo(), request.getReason());
        return PaymentFacadeResponseAssembler.fromCloseResult(result);
    }
}
