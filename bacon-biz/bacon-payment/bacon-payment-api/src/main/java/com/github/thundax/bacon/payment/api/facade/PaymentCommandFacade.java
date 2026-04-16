package com.github.thundax.bacon.payment.api.facade;

import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;

public interface PaymentCommandFacade {

    PaymentCreateFacadeResponse createPayment(PaymentCreateFacadeRequest request);

    PaymentCloseFacadeResponse closePayment(PaymentCloseFacadeRequest request);
}
