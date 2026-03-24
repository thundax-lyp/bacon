package com.github.thundax.bacon.common.core.service;

/**
 * 提供 RSA 密钥生成、加密和解密能力。
 */
public interface RsaCryptoService {

    RsaKeyPair generateKeyPair();

    String encrypt(String plainText, String publicKey);

    String decrypt(String cipherText, String privateKey);
}
