package com.github.thundax.bacon.common.web.advice;

import com.github.thundax.bacon.common.core.exception.BaconException;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.ForbiddenException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.exception.UnauthorizedException;
import com.github.thundax.bacon.common.web.ApiResponse;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 为业务控制器统一转换领域异常和常见参数异常。
 */
@RestControllerAdvice(annotations = WrappedApiController.class)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(BadRequestException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return ApiResponse.fail("BAD_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
        return ApiResponse.fail("BAD_REQUEST",
                "Missing required parameter: " + exception.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ApiResponse.fail("BAD_REQUEST", "Invalid parameter: " + exception.getName());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null) {
            return ApiResponse.fail("BAD_REQUEST", fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        return ApiResponse.fail("BAD_REQUEST", "Request binding failed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null) {
            return ApiResponse.fail("BAD_REQUEST", fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        return ApiResponse.fail("BAD_REQUEST", "Request validation failed");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHandlerMethodValidation(HandlerMethodValidationException exception) {
        if (!exception.getParameterValidationResults().isEmpty()
                && !exception.getParameterValidationResults().get(0).getResolvableErrors().isEmpty()) {
            String message = exception.getParameterValidationResults().get(0).getResolvableErrors().get(0)
                    .getDefaultMessage();
            if (message != null && !message.isBlank()) {
                return ApiResponse.fail("BAD_REQUEST", message);
            }
        }
        return ApiResponse.fail("BAD_REQUEST", "Request validation failed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return ApiResponse.fail("BAD_REQUEST", "Request body is invalid or unreadable");
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleForbidden(ForbiddenException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(NotFoundException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleConflict(ConflictException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(BaconException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleBaconException(BaconException exception) {
        log.error("Unhandled bacon exception", exception);
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ApiResponse.internalServerError();
    }
}
