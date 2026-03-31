package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.List;

public record UserImportRequest(String tenantId, List<UserImportItem> items) {
}
