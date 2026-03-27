package com.github.thundax.bacon.payment.infra.facade.remote.impl;

import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentRemoteExceptionTranslatorTest {

    @Test
    void shouldTranslateHttpStatusToDomainError() {
        RuntimeException translated = PaymentRemoteExceptionTranslator.translate("createPayment",
                HttpClientErrorException.create(HttpStatusCode.valueOf(404), "not-found", HttpHeaders.EMPTY,
                        new byte[0], null));

        assertEquals(PaymentErrorCode.PAYMENT_REMOTE_NOT_FOUND.code(), ((PaymentDomainException) translated).getCode());
    }

    @Test
    void shouldTranslateCircuitOpenToDomainError() {
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("paymentRemoteTest");
        RuntimeException translated = PaymentRemoteExceptionTranslator.translate("createPayment",
                CallNotPermittedException.createCallNotPermittedException(circuitBreaker));

        assertEquals(PaymentErrorCode.PAYMENT_REMOTE_CIRCUIT_OPEN.code(), ((PaymentDomainException) translated).getCode());
    }

    @Test
    void shouldTranslateUnknownToUnavailable() {
        RuntimeException translated = PaymentRemoteExceptionTranslator.translate("createPayment",
                new RuntimeException("boom"));

        assertEquals(PaymentErrorCode.PAYMENT_REMOTE_UNAVAILABLE.code(), ((PaymentDomainException) translated).getCode());
    }
}
