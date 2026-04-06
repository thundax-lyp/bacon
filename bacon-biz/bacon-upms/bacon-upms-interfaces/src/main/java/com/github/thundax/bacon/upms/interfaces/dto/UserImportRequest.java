package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.List;

public record UserImportRequest(String tenantCode, List<UserImportItem> items) {
}
