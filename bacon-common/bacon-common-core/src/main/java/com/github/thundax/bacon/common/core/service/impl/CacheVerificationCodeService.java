package com.github.thundax.bacon.common.core.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 基于 JetCache 的验证码服务，适用于短信、邮件、图形验证码等短时一次性验证码场景。
 */
@Service
public class CacheVerificationCodeService implements VerificationCodeService {

    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private static final String CACHE_NAME = "verificationCode:";

    @CreateCache(name = CACHE_NAME, cacheType = CacheType.REMOTE, expire = 300, timeUnit = TimeUnit.SECONDS)
    private Cache<String, String> verificationCodeCache;

    @Override
    public String generateCode(String scene, String target) {
        return generateCode(scene, target, DEFAULT_CODE_LENGTH, DEFAULT_TTL);
    }

    @Override
    public String generateCode(String scene, String target, int length, Duration ttl) {
        if (length <= 0) {
            throw new BadRequestException("Verification code length must be greater than zero");
        }
        String code = RandomUtil.randomNumbers(length);
        saveCode(scene, target, code, ttl);
        return code;
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
}
