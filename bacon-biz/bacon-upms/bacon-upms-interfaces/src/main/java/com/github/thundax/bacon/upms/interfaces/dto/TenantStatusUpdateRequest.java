package com.github.thundax.bacon.upms.interfaces.dto;

import com.github.thundax.bacon.upms.api.enums.TenantStatusEnum;

public record TenantStatusUpdateRequest(TenantStatusEnum status) {
}
