package com.github.thundax.bacon.auth.application.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.github.thundax.bacon.auth.application.result.PasswordLoginChallengeResult;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.service.RsaCryptoService;
import com.github.thundax.bacon.common.core.service.RsaKeyPair;
import com.github.thundax.bacon.common.core.service.VerificationCodeImage;
import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LoginSecurityApplicationService {

    private static final String LOGIN_PASSWORD_CAPTCHA_SCENE = "LOGIN_PASSWORD_CAPTCHA";
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);
    private static final int DEFAULT_CACHE_LIMIT = 10_000;

    private final Cache<String, String> loginPasswordPrivateKeyCache =
            LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                    .limit(DEFAULT_CACHE_LIMIT)
                    .expireAfterWrite(CHALLENGE_TTL.toSeconds(), TimeUnit.SECONDS)
                    .buildCache();

    private final VerificationCodeService verificationCodeService;
    private final RsaCryptoService rsaCryptoService;

    public LoginSecurityApplicationService(
            VerificationCodeService verificationCodeService, RsaCryptoService rsaCryptoService) {
        this.verificationCodeService = verificationCodeService;
        this.rsaCryptoService = rsaCryptoService;
    }

    public PasswordLoginChallengeResult issuePasswordLoginChallenge() {
        String captchaKey = UUID.randomUUID().toString();
        String rsaKeyId = UUID.randomUUID().toString();
        VerificationCodeImage captchaImage = verificationCodeService.generateImageCode(
                LOGIN_PASSWORD_CAPTCHA_SCENE, captchaKey, 160, 48, 6, 40, CHALLENGE_TTL);
        RsaKeyPair rsaKeyPair = rsaCryptoService.generateKeyPair();
        // 登录 challenge 把验证码和 RSA 私钥拆开保存：验证码走验证码服务，私钥只做短期一次性解密用途。
        loginPasswordPrivateKeyCache.put(
                rsaKeyId, rsaKeyPair.getPrivateKey(), CHALLENGE_TTL.toSeconds(), TimeUnit.SECONDS);
        return new PasswordLoginChallengeResult(
                captchaKey,
                captchaImage.getImageBase64Data(),
                CHALLENGE_TTL.toSeconds(),
                rsaKeyId,
                rsaKeyPair.getPublicKey(),
                CHALLENGE_TTL.toSeconds());
    }

    public void verifyPasswordCaptcha(String captchaKey, String captchaCode) {
        if (!verificationCodeService.verifyAndConsume(LOGIN_PASSWORD_CAPTCHA_SCENE, captchaKey, captchaCode)) {
            throw new BadRequestException("Invalid login captcha");
        }
    }

    public String decryptPassword(String rsaKeyId, String encryptedPassword) {
        if (StringUtils.isBlank(rsaKeyId)) {
            throw new BadRequestException("RSA key id must not be blank");
        }
        String privateKey = loginPasswordPrivateKeyCache.get(rsaKeyId);
        // 私钥读取后立即移除，保证同一个 challenge 只能解密一次，避免被重复提交或离线重放。
        loginPasswordPrivateKeyCache.remove(rsaKeyId);
        if (StringUtils.isBlank(privateKey)) {
            throw new BadRequestException("RSA key does not exist or has expired");
        }
        return rsaCryptoService.decrypt(encryptedPassword, privateKey);
    }
}
