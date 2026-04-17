package com.github.thundax.bacon.upms.interfaces.request;

import java.util.List;

public record UserImportRequest(List<UserImportItemRequest> items) {}
