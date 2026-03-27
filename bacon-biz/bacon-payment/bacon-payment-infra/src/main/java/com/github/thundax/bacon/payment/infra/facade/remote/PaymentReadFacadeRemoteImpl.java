package com.github.thundax.bacon.payment.infra.facade.remote;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
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
    public PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo) {
        // 查询链路也沿用同一套 resilience + translator 规则，避免读写远程调用产生不同的失败语义。
        return restClient.get()
                .uri("/providers/payment/{paymentNo}?tenantId={tenantId}", paymentNo, tenantId)
                .retrieve()
                .body(PaymentDetailDTO.class);
    }

    @Override
    @Retry(name = "paymentRemote", fallbackMethod = "getByOrderNoFallback")
    @CircuitBreaker(name = "paymentRemote", fallbackMethod = "getByOrderNoFallback")
    @Bulkhead(name = "paymentRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getByOrderNoFallback")
    public PaymentDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return restClient.get()
                .uri("/providers/payment?tenantId={tenantId}&orderNo={orderNo}", tenantId, orderNo)
                .retrieve()
                .body(PaymentDetailDTO.class);
    }

    @SuppressWarnings("unused")
    private PaymentDetailDTO getByPaymentNoFallback(Long tenantId, String paymentNo, Throwable throwable) {
        throw PaymentRemoteExceptionTranslator.translate("getByPaymentNo", throwable);
    }

    @SuppressWarnings("unused")
    private PaymentDetailDTO getByOrderNoFallback(Long tenantId, String orderNo, Throwable throwable) {
        throw PaymentRemoteExceptionTranslator.translate("getByOrderNo", throwable);
    }
}
