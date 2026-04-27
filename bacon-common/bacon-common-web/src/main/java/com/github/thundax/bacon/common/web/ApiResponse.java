package com.github.thundax.bacon.common.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 通用接口响应包装对象。
 *
 * @param code 结果码
 * @param data 响应数据
 * @param message 结果消息
 * @param requestId 请求标识
 * @param timestamp 响应时间
 * @param <T> 响应数据类型
 */
public record ApiResponse<T>(
        /** 结果码。 */
        String code,
        /** 响应数据。 */
        T data,
        /** 结果消息。 */
        String message,
        /** 请求标识。 */
        String requestId,
        /** 响应时间。 */
        String timestamp) {

    private static final String SUCCESS_CODE = "SUCCESS";
    private static final String SUCCESS_MESSAGE = "success";
    private static final String DEFAULT_FAILURE_CODE = "INTERNAL_SERVER_ERROR";
    private static final String DEFAULT_FAILURE_MESSAGE = "Internal server error";
    private static final String REQUEST_ID_PARAMETER = "requestId";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(SUCCESS_CODE, data, SUCCESS_MESSAGE, currentRequestId(), currentTimestamp());
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(code, null, message, currentRequestId(), currentTimestamp());
    }

    public static ApiResponse<Void> fail(String message) {
        return fail(DEFAULT_FAILURE_CODE, message);
    }

    public static ApiResponse<Void> internalServerError() {
        return fail(DEFAULT_FAILURE_CODE, DEFAULT_FAILURE_MESSAGE);
    }

    private static String currentRequestId() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String requestId = firstNotBlank(
                    request.getParameter(REQUEST_ID_PARAMETER),
                    request.getHeader(REQUEST_ID_HEADER));
            if (requestId != null) {
                return requestId;
            }
        }
        return UUID.randomUUID().toString();
    }

    private static String currentTimestamp() {
        return Instant.now().toString();
    }

    private static String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
