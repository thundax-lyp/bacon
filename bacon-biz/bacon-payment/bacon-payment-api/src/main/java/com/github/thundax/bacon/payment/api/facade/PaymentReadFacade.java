package com.github.thundax.bacon.payment.api.facade;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;

public interface PaymentReadFacade {

    PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo);

    PaymentDetailDTO getByOrderNo(Long tenantId, String orderNo);
}
