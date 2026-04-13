package com.github.thundax.bacon.common.core.service.impl;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.service.RsaCryptoService;
import com.github.thundax.bacon.common.core.service.RsaKeyPair;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 基于 JCA 的 RSA 服务，提供登录场景所需的密钥生成与加解密能力。
 */
@Service
public class DefaultRsaCryptoServiceImpl implements RsaCryptoService {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 2048;

    @Override
    public RsaKeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return new RsaKeyPair(
                    encode(keyPair.getPublic().getEncoded()),
                    encode(keyPair.getPrivate().getEncoded()));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    @Override
    public String encrypt(String plainText, String publicKey) {
        if (StringUtils.isBlank(plainText)) {
            throw new BadRequestException("RSA plain text must not be blank");
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, resolvePublicKey(publicKey));
            return encode(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new BadRequestException("Failed to encrypt by RSA", ex);
        }
    }

    @Override
    public String decrypt(String cipherText, String privateKey) {
        if (StringUtils.isBlank(cipherText)) {
            throw new BadRequestException("RSA cipher text must not be blank");
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, resolvePrivateKey(privateKey));
            byte[] plainBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new BadRequestException("Failed to decrypt by RSA", ex);
        }
    }

    private PublicKey resolvePublicKey(String publicKey) throws GeneralSecurityException {
        if (StringUtils.isBlank(publicKey)) {
            throw new BadRequestException("RSA public key must not be blank");
        }
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        return KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec);
    }

    private PrivateKey resolvePrivateKey(String privateKey) throws GeneralSecurityException {
        if (StringUtils.isBlank(privateKey)) {
            throw new BadRequestException("RSA private key must not be blank");
        }
        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec);
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
