package com.github.thundax.bacon.order.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
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

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public OrderCommandFacadeRemoteImpl(RestClientFactory restClientFactory,
                                        @Value("${bacon.remote.order-base-url:http://127.0.0.1:8083/api}") String baseUrl,
                                        @Value("${bacon.remote.order.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount,
                         Instant paidTime) {
        // 支付结果回写是命令语义；远程失败必须显式暴露，否则 payment 无法感知 order 侧未完成收口。
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
        // 失败回写与成功回写对称处理，保持 payment 对 order 的远程语义一致。
        restClient.post()
                .uri("/providers/orders/mark-payment-failed?tenantId={tenantId}&orderNo={orderNo}&paymentNo={paymentNo}"
                                + "&reason={reason}&channelStatus={channelStatus}&failedTime={failedTime}",
                        tenantId, orderNo, paymentNo, reason, channelStatus, failedTime)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void closeExpiredOrder(Long tenantId, String orderNo, String reason) {
        // 超时关单是后台补偿命令，不返回 DTO；只要远端未明确成功，就让调用方继续保留重试机会。
        restClient.post()
                .uri("/providers/orders/close-expired?tenantId={tenantId}&orderNo={orderNo}&reason={reason}",
                        tenantId, orderNo, reason)
                .retrieve()
                .toBodilessEntity();
    }
}
