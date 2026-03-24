package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.service.impl.CacheVerificationCodeService;
import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import org.junit.jupiter.api.Test;

class VerificationCodeConfigurationTest {

    private final VerificationCodeConfiguration verificationCodeConfiguration = new VerificationCodeConfiguration();

    @Test
    void shouldCreateVerificationCodeServiceBean() {
        VerificationCodeService verificationCodeService = verificationCodeConfiguration.verificationCodeService();

        assertThat(verificationCodeService).isInstanceOf(CacheVerificationCodeService.class);
    }
}
