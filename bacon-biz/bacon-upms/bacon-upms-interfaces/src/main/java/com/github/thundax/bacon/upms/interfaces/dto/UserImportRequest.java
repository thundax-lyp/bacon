package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.List;

public record UserImportRequest(Long tenantId, List<UserImportItem> items) {
}
