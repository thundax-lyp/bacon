package com.github.thundax.bacon.order.api.facade;

import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;

public interface OrderCommandFacade {

    void markPaid(OrderMarkPaidFacadeRequest request);

    void markPaymentFailed(OrderMarkPaymentFailedFacadeRequest request);

    void closeExpiredOrder(OrderCloseExpiredFacadeRequest request);
}
