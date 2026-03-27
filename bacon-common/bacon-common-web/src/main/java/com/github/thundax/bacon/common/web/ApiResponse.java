package com.github.thundax.bacon.common.web;

/**
 * 通用接口响应包装对象。
 *
 * @param code 结果码
 * @param data 响应数据
 * @param message 结果消息
 * @param <T> 响应数据类型
 */
public record ApiResponse<T>(
        /** 结果码。 */
        String code,
        /** 响应数据。 */
        T data,
        /** 结果消息。 */
        String message) {

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
