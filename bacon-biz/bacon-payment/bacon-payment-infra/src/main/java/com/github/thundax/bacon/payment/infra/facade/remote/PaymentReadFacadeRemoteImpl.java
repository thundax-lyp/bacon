package com.github.thundax.bacon.payment.infra.facade.remote;

import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import com.github.thundax.bacon.payment.api.request.PaymentGetByOrderNoFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentGetByPaymentNoFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentDetailFacadeResponse;
import com.github.thundax.bacon.payment.infra.facade.remote.impl.PaymentRemoteExceptionTranslator;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class PaymentReadFacadeRemoteImpl implements PaymentReadFacade {

    private final RestClient restClient;

    public PaymentReadFacadeRemoteImpl(@Qualifier("paymentRemoteRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Retry(name = "paymentRemote", fallbackMethod = "getByPaymentNoFallback")
    @CircuitBreaker(name = "paymentRemote", fallbackMethod = "getByPaymentNoFallback")
    @Bulkhead(name = "paymentRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getByPaymentNoFallback")
    public PaymentDetailFacadeResponse getByPaymentNo(PaymentGetByPaymentNoFacadeRequest request) {
        // 查询链路也沿用同一套 resilience + translator 规则，避免读写远程调用产生不同的失败语义。
        return restClient
                .get()
                .uri("/providers/payment/queries/detail?paymentNo={paymentNo}", request.getPaymentNo())
                .retrieve()
                .body(PaymentDetailFacadeResponse.class);
    }

    @Override
    @Retry(name = "paymentRemote", fallbackMethod = "getByOrderNoFallback")
    @CircuitBreaker(name = "paymentRemote", fallbackMethod = "getByOrderNoFallback")
    @Bulkhead(name = "paymentRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getByOrderNoFallback")
    public PaymentDetailFacadeResponse getByOrderNo(PaymentGetByOrderNoFacadeRequest request) {
        return restClient
                .get()
                .uri("/providers/payment/queries/by-order?orderNo={orderNo}", request.getOrderNo())
                .retrieve()
                .body(PaymentDetailFacadeResponse.class);
    }

    @SuppressWarnings("unused")
    private PaymentDetailFacadeResponse getByPaymentNoFallback(
            PaymentGetByPaymentNoFacadeRequest request, Throwable throwable) {
        throw PaymentRemoteExceptionTranslator.translate("getByPaymentNo", throwable);
    }

    @SuppressWarnings("unused")
    private PaymentDetailFacadeResponse getByOrderNoFallback(
            PaymentGetByOrderNoFacadeRequest request, Throwable throwable) {
        throw PaymentRemoteExceptionTranslator.translate("getByOrderNo", throwable);
    }
}
