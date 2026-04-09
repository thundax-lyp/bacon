package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import com.github.thundax.bacon.common.core.service.impl.CacheVerificationCodeServiceImpl;
import org.junit.jupiter.api.Test;

class VerificationCodeAutoConfigurationTest {

    private final VerificationCodeAutoConfiguration verificationCodeAutoConfiguration =
            new VerificationCodeAutoConfiguration();

    @Test
    void shouldCreateVerificationCodeServiceBean() {
        VerificationCodeService verificationCodeService = verificationCodeAutoConfiguration.verificationCodeService();

        assertThat(verificationCodeService).isInstanceOf(CacheVerificationCodeServiceImpl.class);
    }
}
