package com.github.thundax.bacon.auth.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsLoginRequest {

    private String phone;
    private String smsCaptcha;
}
