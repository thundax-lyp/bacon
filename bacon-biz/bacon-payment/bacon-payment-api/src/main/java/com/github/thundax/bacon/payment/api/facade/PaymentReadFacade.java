package com.github.thundax.bacon.payment.api.facade;

import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;

public interface PaymentReadFacade {

    PaymentDetailFacadeResponse getByPaymentNo(PaymentGetByPaymentNoFacadeRequest request);

    PaymentDetailFacadeResponse getByOrderNo(PaymentGetByOrderNoFacadeRequest request);
}
