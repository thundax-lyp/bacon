package com.github.thundax.bacon.payment.api.facade;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;

public interface PaymentReadFacade {

    PaymentDetailDTO getByPaymentNo(String paymentNo);

    PaymentDetailDTO getByOrderNo(String orderNo);
}
