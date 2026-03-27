package com.github.thundax.bacon.payment.infra.facade.remote;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.infra.facade.remote.impl.PaymentRemoteExceptionTranslator;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class PaymentCommandFacadeRemoteImpl implements PaymentCommandFacade {

    private final RestClient restClient;

    public PaymentCommandFacadeRemoteImpl(@Qualifier("paymentRemoteRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Retry(name = "paymentRemote", fallbackMethod = "createPaymentFallback")
    @CircuitBreaker(name = "paymentRemote", fallbackMethod = "createPaymentFallback")
    @Bulkhead(name = "paymentRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "createPaymentFallback")
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
    @Retry(name = "paymentRemote", fallbackMethod = "closePaymentFallback")
    @CircuitBreaker(name = "paymentRemote", fallbackMethod = "closePaymentFallback")
    @Bulkhead(name = "paymentRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "closePaymentFallback")
    public PaymentCloseResultDTO closePayment(Long tenantId, String paymentNo, String reason) {
        return restClient.post()
                .uri("/providers/payment/close?tenantId={tenantId}&paymentNo={paymentNo}&reason={reason}",
                        tenantId, paymentNo, reason)
                .retrieve()
                .body(PaymentCloseResultDTO.class);
    }

    @SuppressWarnings("unused")
    private PaymentCreateResultDTO createPaymentFallback(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                         String channelCode, String subject, Instant expiredAt,
                                                         Throwable throwable) {
        throw PaymentRemoteExceptionTranslator.translate("createPayment", throwable);
    }

    @SuppressWarnings("unused")
    private PaymentCloseResultDTO closePaymentFallback(Long tenantId, String paymentNo, String reason,
                                                       Throwable throwable) {
        throw PaymentRemoteExceptionTranslator.translate("closePayment", throwable);
    }
}
