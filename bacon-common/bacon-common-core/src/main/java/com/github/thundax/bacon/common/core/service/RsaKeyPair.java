package com.github.thundax.bacon.common.core.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 承载 RSA 公私钥对内容。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RsaKeyPair {

    private String publicKey;
    private String privateKey;
}
