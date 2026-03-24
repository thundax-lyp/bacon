package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.service.impl.CacheVerificationCodeService;
import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import org.junit.jupiter.api.Test;

class VerificationCodeAutoConfigurationTest {

    private final VerificationCodeAutoConfiguration verificationCodeAutoConfiguration = new VerificationCodeAutoConfiguration();

    @Test
    void shouldCreateVerificationCodeServiceBean() {
        VerificationCodeService verificationCodeService = verificationCodeAutoConfiguration.verificationCodeService();

        assertThat(verificationCodeService).isInstanceOf(CacheVerificationCodeService.class);
    }
}
