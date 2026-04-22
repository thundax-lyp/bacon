package com.github.thundax.bacon.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsLoginCommand {

    private String phone;
    private String smsCaptcha;
}
