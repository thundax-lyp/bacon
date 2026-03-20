package com.github.thundax.bacon.payment.infra.rpc;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class PaymentReadFacadeRemoteImpl implements PaymentReadFacade {

    private final RestClient restClient;

    public PaymentReadFacadeRemoteImpl(@Value("${bacon.remote.payment-base-url:http://localhost:8086}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo) {
        return restClient.get()
                .uri("/providers/payment/{paymentNo}?tenantId={tenantId}", paymentNo, tenantId)
                .retrieve()
                .body(PaymentDetailDTO.class);
    }

    @Override
    public PaymentDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return restClient.get()
                .uri("/providers/payment?tenantId={tenantId}&orderNo={orderNo}", tenantId, orderNo)
                .retrieve()
                .body(PaymentDetailDTO.class);
    }
}
