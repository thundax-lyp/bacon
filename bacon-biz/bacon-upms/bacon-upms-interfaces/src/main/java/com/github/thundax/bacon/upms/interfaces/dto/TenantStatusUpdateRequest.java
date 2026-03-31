package com.github.thundax.bacon.upms.interfaces.dto;

import com.github.thundax.bacon.upms.api.enums.UpmsStatusEnum;

public record TenantStatusUpdateRequest(UpmsStatusEnum status) {
}
