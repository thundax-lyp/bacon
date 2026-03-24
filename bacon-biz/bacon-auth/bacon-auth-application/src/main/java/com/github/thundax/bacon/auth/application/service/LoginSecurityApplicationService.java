package com.github.thundax.bacon.auth.application.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.bacon.auth.application.dto.PasswordLoginChallengeResult;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.service.RsaCryptoService;
import com.github.thundax.bacon.common.core.service.RsaKeyPair;
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
    private static final String PRIVATE_KEY_CACHE_NAME = "loginPasswordPrivateKey:";

    @CreateCache(name = PRIVATE_KEY_CACHE_NAME, cacheType = CacheType.REMOTE, expire = 300, timeUnit = TimeUnit.SECONDS)
    private Cache<String, String> loginPasswordPrivateKeyCache;

    private final VerificationCodeService verificationCodeService;
    private final RsaCryptoService rsaCryptoService;

    public LoginSecurityApplicationService(VerificationCodeService verificationCodeService,
                                           RsaCryptoService rsaCryptoService) {
        this.verificationCodeService = verificationCodeService;
        this.rsaCryptoService = rsaCryptoService;
    }

    public PasswordLoginChallengeResult issuePasswordLoginChallenge() {
        String captchaKey = UUID.randomUUID().toString();
        String rsaKeyId = UUID.randomUUID().toString();
        String captchaCode = verificationCodeService.generateCode(LOGIN_PASSWORD_CAPTCHA_SCENE, captchaKey, 6, CHALLENGE_TTL);
        RsaKeyPair rsaKeyPair = rsaCryptoService.generateKeyPair();
        loginPasswordPrivateKeyCache.put(rsaKeyId, rsaKeyPair.getPrivateKey(), CHALLENGE_TTL.toSeconds(), TimeUnit.SECONDS);
        return new PasswordLoginChallengeResult(captchaKey, captchaCode, CHALLENGE_TTL.toSeconds(),
                rsaKeyId, rsaKeyPair.getPublicKey(), CHALLENGE_TTL.toSeconds());
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
        loginPasswordPrivateKeyCache.remove(rsaKeyId);
        if (StringUtils.isBlank(privateKey)) {
            throw new BadRequestException("RSA key does not exist or has expired");
        }
        return rsaCryptoService.decrypt(encryptedPassword, privateKey);
    }
}
