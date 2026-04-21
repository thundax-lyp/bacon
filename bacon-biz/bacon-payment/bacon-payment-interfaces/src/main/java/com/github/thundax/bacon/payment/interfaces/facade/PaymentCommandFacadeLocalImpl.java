package com.github.thundax.bacon.payment.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.interfaces.assembler.PaymentInterfaceAssembler;
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
        return PaymentInterfaceAssembler.toCreateFacadeResponse(
                paymentCreateApplicationService.createPayment(PaymentInterfaceAssembler.toCreateCommand(request)));
    }

    @Override
    public PaymentCloseFacadeResponse closePayment(PaymentCloseFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return PaymentInterfaceAssembler.toCloseFacadeResponse(
                paymentCloseApplicationService.closePayment(PaymentInterfaceAssembler.toCloseCommand(request)));
    }
}
