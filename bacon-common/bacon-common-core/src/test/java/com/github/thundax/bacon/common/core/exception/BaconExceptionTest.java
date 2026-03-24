package com.github.thundax.bacon.common.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BaconExceptionTest {

    @Test
    void shouldExposeCodeAndMessage() {
        BaconException exception = new BaconException("COMMON_ERROR", "common message");

        assertThat(exception.getCode()).isEqualTo("COMMON_ERROR");
        assertThat(exception.getMessage()).isEqualTo("common message");
    }

    @Test
    void shouldUseDefaultCodeForBadRequestException() {
        BadRequestException exception = new BadRequestException("bad request");

        assertThat(exception.getCode()).isEqualTo("BAD_REQUEST");
        assertThat(exception.getMessage()).isEqualTo("bad request");
    }

    @Test
    void shouldUseDefaultCodeForUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException("unauthorized");

        assertThat(exception.getCode()).isEqualTo("UNAUTHORIZED");
        assertThat(exception.getMessage()).isEqualTo("unauthorized");
    }

    @Test
    void shouldUseCustomCodeWhenProvided() {
        NotFoundException exception = new NotFoundException("ORDER_NOT_FOUND", "order missing");

        assertThat(exception.getCode()).isEqualTo("ORDER_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("order missing");
    }

    @Test
    void shouldKeepCauseForSystemException() {
        IllegalStateException cause = new IllegalStateException("boom");
        SystemException exception = new SystemException("system error", cause);

        assertThat(exception.getCode()).isEqualTo("SYSTEM_ERROR");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void shouldUseDefaultCodeForConflictAndForbidden() {
        ConflictException conflictException = new ConflictException("conflict");
        ForbiddenException forbiddenException = new ForbiddenException("forbidden");

        assertThat(conflictException.getCode()).isEqualTo("CONFLICT");
        assertThat(forbiddenException.getCode()).isEqualTo("FORBIDDEN");
    }
}
