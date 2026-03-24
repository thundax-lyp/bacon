package com.github.thundax.bacon.common.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.service.RsaKeyPair;
import org.junit.jupiter.api.Test;

class DefaultRsaCryptoServiceTest {

    private final DefaultRsaCryptoService rsaCryptoService = new DefaultRsaCryptoService();

    @Test
    void shouldEncryptAndDecrypt() {
        RsaKeyPair keyPair = rsaCryptoService.generateKeyPair();

        assertNotNull(keyPair.getPublicKey());
        assertNotNull(keyPair.getPrivateKey());

        String cipherText = rsaCryptoService.encrypt("123456", keyPair.getPublicKey());
        String plainText = rsaCryptoService.decrypt(cipherText, keyPair.getPrivateKey());

        assertEquals("123456", plainText);
    }

    @Test
    void shouldRejectBlankCipherText() {
        RsaKeyPair keyPair = rsaCryptoService.generateKeyPair();

        assertThrows(BadRequestException.class, () -> rsaCryptoService.decrypt("", keyPair.getPrivateKey()));
    }
}
