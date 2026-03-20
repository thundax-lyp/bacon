package com.github.thundax.bacon.payment.infra.rpc;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class PaymentCommandFacadeRemoteImpl implements PaymentCommandFacade {

    private final RestClient restClient;

    public PaymentCommandFacadeRemoteImpl(@Value("${bacon.remote.payment-base-url:http://localhost:8086}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public PaymentCreateResultDTO createPayment(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                String channelCode, String subject, Instant expiredAt) {
        return restClient.post()
                .uri("/providers/payment/create?tenantId={tenantId}&orderNo={orderNo}&userId={userId}"
                                + "&amount={amount}&channelCode={channelCode}&subject={subject}&expiredAt={expiredAt}",
                        tenantId, orderNo, userId, amount, channelCode, subject, expiredAt)
                .retrieve()
                .body(PaymentCreateResultDTO.class);
    }

    @Override
    public PaymentCloseResultDTO closePayment(Long tenantId, String paymentNo, String reason) {
        return restClient.post()
                .uri("/providers/payment/close?tenantId={tenantId}&paymentNo={paymentNo}&reason={reason}",
                        tenantId, paymentNo, reason)
                .retrieve()
                .body(PaymentCloseResultDTO.class);
    }
}
