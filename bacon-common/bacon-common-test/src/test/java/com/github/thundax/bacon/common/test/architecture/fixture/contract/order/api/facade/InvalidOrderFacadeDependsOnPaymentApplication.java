package com.github.thundax.bacon.common.test.architecture.fixture.contract.order.api.facade;

import com.github.thundax.bacon.common.test.architecture.fixture.contract.payment.application.service.PaymentApplicationService;

public class InvalidOrderFacadeDependsOnPaymentApplication {

    public PaymentApplicationService expose() {
        return new PaymentApplicationService();
    }
}
