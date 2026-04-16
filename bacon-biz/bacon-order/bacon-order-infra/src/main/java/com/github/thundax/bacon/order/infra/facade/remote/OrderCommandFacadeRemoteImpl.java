package com.github.thundax.bacon.order.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OrderCommandFacadeRemoteImpl implements OrderCommandFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public OrderCommandFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.order-base-url:http://bacon-order-service/api}") String baseUrl,
            @Value("${bacon.remote.order.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public void markPaid(OrderMarkPaidFacadeRequest request) {
        // 支付结果回写是命令语义；远程失败必须显式暴露，否则 payment 无法感知 order 侧未完成收口。
        restClient.post().uri("/providers/order/mark-paid").body(request).retrieve().toBodilessEntity();
    }

    @Override
    public void markPaymentFailed(OrderMarkPaymentFailedFacadeRequest request) {
        // 失败回写与成功回写对称处理，保持 payment 对 order 的远程语义一致。
        restClient
                .post()
                .uri("/providers/order/mark-payment-failed")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void closeExpiredOrder(OrderCloseExpiredFacadeRequest request) {
        // 超时关单是后台补偿命令，不返回 DTO；只要远端未明确成功，就让调用方继续保留重试机会。
        restClient.post().uri("/providers/order/close-expired").body(request).retrieve().toBodilessEntity();
    }
}
