package com.github.thundax.bacon.common.core.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 承载验证码原文与图形数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCodeImage {

    private String code;
    private String imageBase64Data;
}
