package com.github.thundax.bacon.payment.infra.facade.remote;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.infra.facade.remote.impl.PaymentRemoteExceptionTranslator;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    public PaymentCreateResultDTO createPayment(
            String orderNo,
            Long userId,
            BigDecimal amount,
            String channelCode,
            String subject,
            Instant expiredAt) {
        // remote facade 只负责协议转发，不在这里做支付业务兜底；一切失败都交给 fallback 统一翻译。
        return restClient
                .post()
                .uri(
                        "/providers/payment/create?orderNo={orderNo}&userId={userId}"
                                + "&amount={amount}&channelCode={channelCode}&subject={subject}&expiredAt={expiredAt}",
                        orderNo,
                        userId,
                        amount,
                        channelCode,
                        subject,
                        expiredAt)
                .retrieve()
                .body(PaymentCreateResultDTO.class);
    }

    @Override
    @Retry(name = "paymentRemote", fallbackMethod = "closePaymentFallback")
    @CircuitBreaker(name = "paymentRemote", fallbackMethod = "closePaymentFallback")
    @Bulkhead(name = "paymentRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "closePaymentFallback")
    public PaymentCloseResultDTO closePayment(String paymentNo, String reason) {
        return restClient
                .post()
                .uri("/providers/payment/close?paymentNo={paymentNo}&reason={reason}", paymentNo, reason)
                .retrieve()
                .body(PaymentCloseResultDTO.class);
    }

    @SuppressWarnings("unused")
    private PaymentCreateResultDTO createPaymentFallback(
            String orderNo,
            Long userId,
            BigDecimal amount,
            String channelCode,
            String subject,
            Instant expiredAt,
            Throwable throwable) {
        // fallback 的职责不是返回本地默认值，而是把 retry/circuit/bulkhead/http 异常统一收敛成支付领域异常。
        throw PaymentRemoteExceptionTranslator.translate("createPayment", throwable);
    }

    @SuppressWarnings("unused")
    private PaymentCloseResultDTO closePaymentFallback(String paymentNo, String reason, Throwable throwable) {
        throw PaymentRemoteExceptionTranslator.translate("closePayment", throwable);
    }
}
