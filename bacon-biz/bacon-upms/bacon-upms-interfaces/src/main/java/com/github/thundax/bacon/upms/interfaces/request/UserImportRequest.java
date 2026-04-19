package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UserImportRequest(@NotEmpty(message = "items must not be empty") List<@Valid UserImportItemRequest> items) {}
