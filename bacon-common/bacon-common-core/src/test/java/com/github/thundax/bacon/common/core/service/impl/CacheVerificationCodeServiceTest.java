package com.github.thundax.bacon.common.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.service.VerificationCodeImage;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CacheVerificationCodeServiceTest {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    private final CacheVerificationCodeService verificationCodeService = new CacheVerificationCodeService();

    @BeforeEach
    void setUp() {
        Cache<String, String> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(100)
                .expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES)
                .buildCache();
        ReflectionTestUtils.setField(verificationCodeService, "verificationCodeCache", cache);
    }

    @Test
    void shouldGenerateAndStoreVerificationCode() {
        String code = verificationCodeService.generateCode("sms", "13800138000");

        assertThat(code).hasSize(6).containsOnlyDigits();
        assertThat(verificationCodeService.verifyCode("sms", "13800138000", code)).isTrue();
    }

    @Test
    void shouldSupportCustomLengthAndTtl() {
        String code = verificationCodeService.generateCode("email", "user@test.com", 4, Duration.ofMinutes(1));

        assertThat(code).hasSize(4).containsOnlyDigits();
        assertThat(verificationCodeService.verifyCode("email", "user@test.com", code)).isTrue();
    }

    @Test
    void shouldVerifyAndConsumeCode() {
        verificationCodeService.saveCode("sms", "13800138000", "123456", Duration.ofMinutes(5));

        assertThat(verificationCodeService.verifyAndConsume("sms", "13800138000", "123456")).isTrue();
        assertThat(verificationCodeService.verifyCode("sms", "13800138000", "123456")).isFalse();
    }

    @Test
    void shouldRemoveCode() {
        verificationCodeService.saveCode("sms", "13800138000", "123456", Duration.ofMinutes(5));

        verificationCodeService.removeCode("sms", "13800138000");

        assertThat(verificationCodeService.verifyCode("sms", "13800138000", "123456")).isFalse();
    }

    @Test
    void shouldRejectInvalidArguments() {
        assertThatThrownBy(() -> verificationCodeService.saveCode("", "13800138000", "123456", Duration.ofMinutes(5)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> verificationCodeService.generateCode("sms", "13800138000", 0, Duration.ofMinutes(5)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> verificationCodeService.generateImageCode("sms", "13800138000", 0, 48, 4, 10,
                Duration.ofMinutes(5))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> verificationCodeService.saveCode("sms", "13800138000", "123456", Duration.ZERO))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldGenerateImageVerificationCode() {
        VerificationCodeImage image = verificationCodeService.generateImageCode("login", "captcha-key");

        assertThat(image.getCode()).hasSize(6).containsOnlyDigits();
        assertThat(image.getImageBase64Data()).startsWith("data:image/png;base64,");
        assertThat(verificationCodeService.verifyCode("login", "captcha-key", image.getCode())).isTrue();
    }
}
