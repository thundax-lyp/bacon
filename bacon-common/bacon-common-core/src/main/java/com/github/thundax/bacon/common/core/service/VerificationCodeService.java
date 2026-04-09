package com.github.thundax.bacon.common.core.service;

import java.time.Duration;

/**
 * 验证码服务，提供验证码生成、存储、校验和删除能力。
 */
public interface VerificationCodeService {

    String generateCode(String scene, String target);

    String generateCode(String scene, String target, int length, Duration ttl);

    VerificationCodeImage generateImageCode(String scene, String target);

    VerificationCodeImage generateImageCode(
            String scene, String target, int width, int height, int length, int interfereCount, Duration ttl);

    void saveCode(String scene, String target, String code, Duration ttl);

    boolean verifyCode(String scene, String target, String code);

    boolean verifyAndConsume(String scene, String target, String code);

    void removeCode(String scene, String target);
}
