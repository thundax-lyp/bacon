package com.github.thundax.bacon.order.infra.rpc;

import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OrderCommandFacadeRemoteImpl implements OrderCommandFacade {

    private final RestClient restClient;

    public OrderCommandFacadeRemoteImpl(@Value("${bacon.remote.order-base-url:http://localhost:8084}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount,
                         Instant paidTime) {
        restClient.post()
                .uri("/providers/orders/mark-paid?tenantId={tenantId}&orderNo={orderNo}&paymentNo={paymentNo}"
                                + "&channelCode={channelCode}&paidAmount={paidAmount}&paidTime={paidTime}",
                        tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                  Instant failedTime) {
        restClient.post()
                .uri("/providers/orders/mark-payment-failed?tenantId={tenantId}&orderNo={orderNo}&paymentNo={paymentNo}"
                                + "&reason={reason}&channelStatus={channelStatus}&failedTime={failedTime}",
                        tenantId, orderNo, paymentNo, reason, channelStatus, failedTime)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        restClient.post()
                .uri("/providers/orders/close-expired?tenantId={tenantId}&orderNo={orderNo}&reason={reason}",
                        tenantId, orderNo, reason)
                .retrieve()
                .toBodilessEntity();
    }
}
