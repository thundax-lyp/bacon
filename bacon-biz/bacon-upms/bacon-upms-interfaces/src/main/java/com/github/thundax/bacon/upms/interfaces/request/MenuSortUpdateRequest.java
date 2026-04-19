package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.NotNull;

public record MenuSortUpdateRequest(@NotNull(message = "sort must not be null") Integer sort) {}
