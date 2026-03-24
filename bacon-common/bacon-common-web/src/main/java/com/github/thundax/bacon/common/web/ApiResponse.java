package com.github.thundax.bacon.common.web;

public record ApiResponse<T>(String code, T data, String message) {

    private static final String SUCCESS_CODE = "SUCCESS";
    private static final String SUCCESS_MESSAGE = "success";
    private static final String DEFAULT_FAILURE_CODE = "INTERNAL_SERVER_ERROR";
    private static final String DEFAULT_FAILURE_MESSAGE = "Internal server error";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(SUCCESS_CODE, data, SUCCESS_MESSAGE);
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    public static ApiResponse<Void> fail(String message) {
        return fail(DEFAULT_FAILURE_CODE, message);
    }

    public static ApiResponse<Void> internalServerError() {
        return fail(DEFAULT_FAILURE_CODE, DEFAULT_FAILURE_MESSAGE);
    }
}
