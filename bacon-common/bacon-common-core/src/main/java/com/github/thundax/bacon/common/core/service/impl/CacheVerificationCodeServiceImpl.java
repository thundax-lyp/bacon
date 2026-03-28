package com.github.thundax.bacon.common.core.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.service.VerificationCodeImage;
import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 * 基于 JetCache 的验证码服务，适用于短信、邮件、图形验证码等短时一次性验证码场景。
 */
public class CacheVerificationCodeServiceImpl implements VerificationCodeService {

    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private static final String CACHE_NAME = "verificationCode:";
    private static final String NUMBERS = "0123456789";
    private static final int DEFAULT_CAPTCHA_WIDTH = 160;
    private static final int DEFAULT_CAPTCHA_HEIGHT = 48;
    private static final int DEFAULT_INTERFERE_COUNT = 40;
    private static final int DEFAULT_CACHE_LIMIT = 10_000;

    private final Cache<String, String> verificationCodeCache;

    public CacheVerificationCodeServiceImpl() {
        this(LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(DEFAULT_CACHE_LIMIT)
                .expireAfterWrite(DEFAULT_TTL.toSeconds(), TimeUnit.SECONDS)
                .buildCache());
    }

    CacheVerificationCodeServiceImpl(Cache<String, String> verificationCodeCache) {
        this.verificationCodeCache = verificationCodeCache;
    }

    @Override
    public String generateCode(String scene, String target) {
        return generateCode(scene, target, DEFAULT_CODE_LENGTH, DEFAULT_TTL);
    }

    @Override
    public String generateCode(String scene, String target, int length, Duration ttl) {
        if (length <= 0) {
            throw new BadRequestException("Verification code length must be greater than zero");
        }
        LineCaptcha captcha = createLineCaptcha(
                length, DEFAULT_CAPTCHA_WIDTH, DEFAULT_CAPTCHA_HEIGHT, DEFAULT_INTERFERE_COUNT
        );
        String code = captcha.getCode();
        saveCode(scene, target, code, ttl);
        return code;
    }

    @Override
    public VerificationCodeImage generateImageCode(String scene, String target) {
        return generateImageCode(scene, target, DEFAULT_CAPTCHA_WIDTH, DEFAULT_CAPTCHA_HEIGHT,
                DEFAULT_CODE_LENGTH, DEFAULT_INTERFERE_COUNT, DEFAULT_TTL);
    }

    @Override
    public VerificationCodeImage generateImageCode(String scene, String target, int width, int height, int length,
                                                   int interfereCount, Duration ttl) {
        if (width <= 0 || height <= 0) {
            throw new BadRequestException("Verification code image size must be greater than zero");
        }
        if (interfereCount < 0) {
            throw new BadRequestException("Verification code interfere count must not be negative");
        }
        LineCaptcha captcha = createLineCaptcha(length, width, height, interfereCount);
        saveCode(scene, target, captcha.getCode(), ttl);
        return new VerificationCodeImage(captcha.getCode(), captcha.getImageBase64Data());
    }

    @Override
    public void saveCode(String scene, String target, String code, Duration ttl) {
        validateSceneAndTarget(scene, target);
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("Verification code must not be blank");
        }
        Duration effectiveTtl = Objects.requireNonNullElse(ttl, DEFAULT_TTL);
        if (effectiveTtl.isZero() || effectiveTtl.isNegative()) {
            throw new BadRequestException("Verification code ttl must be greater than zero");
        }
        verificationCodeCache.put(buildKey(scene, target), code, effectiveTtl.toSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public boolean verifyCode(String scene, String target, String code) {
        validateSceneAndTarget(scene, target);
        if (StringUtils.isBlank(code)) {
            return false;
        }
        return StringUtils.equals(code, verificationCodeCache.get(buildKey(scene, target)));
    }

    @Override
    public boolean verifyAndConsume(String scene, String target, String code) {
        boolean verified = verifyCode(scene, target, code);
        if (verified) {
            removeCode(scene, target);
        }
        return verified;
    }

    @Override
    public void removeCode(String scene, String target) {
        validateSceneAndTarget(scene, target);
        verificationCodeCache.remove(buildKey(scene, target));
    }

    private void validateSceneAndTarget(String scene, String target) {
        if (StringUtils.isBlank(scene)) {
            throw new BadRequestException("Verification code scene must not be blank");
        }
        if (StringUtils.isBlank(target)) {
            throw new BadRequestException("Verification code target must not be blank");
        }
    }

    private String buildKey(String scene, String target) {
        return scene + ":" + target;
    }

    private LineCaptcha createLineCaptcha(int length, int width, int height, int interfereCount) {
        if (length <= 0) {
            throw new BadRequestException("Verification code length must be greater than zero");
        }
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(
                width, height, new RandomGenerator(NUMBERS, length), interfereCount
        );
        captcha.createCode();
        return captcha;
    }
}
