package com.github.thundax.bacon.common.web;

public record ApiResponse<T>(boolean success, T data, String message) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "OK");
    }
}
