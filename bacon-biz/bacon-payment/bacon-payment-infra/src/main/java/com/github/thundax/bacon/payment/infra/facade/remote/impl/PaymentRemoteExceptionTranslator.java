package com.github.thundax.bacon.payment.infra.facade.remote.impl;

import com.github.thundax.bacon.common.core.exception.BaconException;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.RestClientResponseException;

public final class PaymentRemoteExceptionTranslator {

    private PaymentRemoteExceptionTranslator() {
    }

    public static RuntimeException translate(String operation, Throwable throwable) {
        if (throwable instanceof BaconException baconException) {
            return baconException;
        }
        if (throwable instanceof CallNotPermittedException) {
            return new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_CIRCUIT_OPEN, operation);
        }
        if (throwable.getClass().getSimpleName().contains("Bulkhead")) {
            return new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_BULKHEAD_FULL, operation);
        }
        if (throwable instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            return switch (statusCode) {
                case 400 -> new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_BAD_REQUEST, operation);
                case 401 -> new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_UNAUTHORIZED, operation);
                case 403 -> new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_FORBIDDEN, operation);
                case 404 -> new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_NOT_FOUND, operation);
                case 409 -> new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_CONFLICT, operation);
                default -> new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_ERROR,
                        operation + ", status=" + statusCode);
            };
        }
        return new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_UNAVAILABLE, operation);
    }
}
