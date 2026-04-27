package com.github.thundax.bacon.product.interfaces.request;

import jakarta.validation.constraints.NotNull;

public record ChangeProductStatusRequest(@NotNull Long expectedVersion) {}
