package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;

public record UserExportQuery(String account, String name, String phone, UserStatus status) {}
