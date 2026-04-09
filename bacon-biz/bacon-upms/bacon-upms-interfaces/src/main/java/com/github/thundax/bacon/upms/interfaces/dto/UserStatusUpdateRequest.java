package com.github.thundax.bacon.upms.interfaces.dto;

import com.github.thundax.bacon.upms.api.enums.EnableStatusEnum;

public record UserStatusUpdateRequest(String tenantCode, EnableStatusEnum status) {}
