package com.github.thundax.bacon.payment.infra.facade.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PaymentRemoteFacadeFallbackTest {

    @Test
    void commandFacadeFallbackShouldTranslateToDomainError() throws Exception {
        PaymentCommandFacadeRemoteImpl facade = new PaymentCommandFacadeRemoteImpl(null);
        Method method = PaymentCommandFacadeRemoteImpl.class.getDeclaredMethod(
                "createPaymentFallback",
                String.class,
                Long.class,
                BigDecimal.class,
                String.class,
                String.class,
                Instant.class,
                Throwable.class);
        method.setAccessible(true);

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(
                        facade,
                        "ORD-10001",
                        2001L,
                        BigDecimal.TEN,
                        "MOCK",
                        "test",
                        Instant.parse("2026-03-27T10:30:00Z"),
                        new RuntimeException("boom")));

        Throwable cause = thrown.getTargetException();
        assertEquals(PaymentErrorCode.PAYMENT_REMOTE_UNAVAILABLE.code(), ((PaymentDomainException) cause).getCode());
    }

    @Test
    void readFacadeFallbackShouldTranslateToDomainError() throws Exception {
        PaymentReadFacadeRemoteImpl facade = new PaymentReadFacadeRemoteImpl(null);
        Method method = PaymentReadFacadeRemoteImpl.class.getDeclaredMethod(
                "getByPaymentNoFallback", String.class, Throwable.class);
        method.setAccessible(true);

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(facade, "PAY-10001", new RuntimeException("boom")));

        Throwable cause = thrown.getTargetException();
        assertEquals(PaymentErrorCode.PAYMENT_REMOTE_UNAVAILABLE.code(), ((PaymentDomainException) cause).getCode());
    }
}
