package com.github.thundax.bacon.common.log.aspect;

import com.github.thundax.bacon.common.log.LogFieldNames;
import com.github.thundax.bacon.common.log.LogResult;
import com.github.thundax.bacon.common.log.annotation.SysLog;
import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import com.github.thundax.bacon.common.log.producer.SysLogMessageProducer;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class SysLogAspect {

    private final SysLogMessageProducer sysLogMessageProducer;

    public SysLogAspect(SysLogMessageProducer sysLogMessageProducer) {
        this.sysLogMessageProducer = sysLogMessageProducer;
    }

    @Around("@annotation(com.github.thundax.bacon.common.log.annotation.SysLog)"
            + " || @within(com.github.thundax.bacon.common.log.annotation.SysLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        SysLog sysLog = resolveSysLog(joinPoint);
        if (sysLog == null) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        Throwable throwable = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            sysLogMessageProducer.send(buildMessage(joinPoint, sysLog, startTime, throwable));
        }
    }

    private SysLogDTO buildMessage(ProceedingJoinPoint joinPoint, SysLog sysLog, long startTime, Throwable throwable) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes instanceof ServletRequestAttributes servletRequestAttributes
                ? servletRequestAttributes.getRequest()
                : null;
        long costMs = System.currentTimeMillis() - startTime;

        String traceId = readRequestValue(request, LogFieldNames.TRACE_ID, "X-Trace-Id");
        String requestId = readRequestValue(request, LogFieldNames.REQUEST_ID, "X-Request-Id");
        Long tenantId = readTenantId(request);
        String operatorId = readRequestValue(request, LogFieldNames.USER_ID, "X-User-Id");
        String operatorName = request == null ? null : request.getRemoteUser();
        String clientIp = request == null ? null : request.getRemoteAddr();
        String requestUri = request == null ? null : request.getRequestURI();
        String httpMethod = request == null ? null : request.getMethod();

        return new SysLogDTO(
                traceId == null ? UUID.randomUUID().toString() : traceId,
                requestId == null ? UUID.randomUUID().toString() : requestId,
                sysLog.module(),
                sysLog.action(),
                sysLog.eventType(),
                throwable == null ? LogResult.SUCCESS : LogResult.FAILURE,
                operatorId,
                operatorName,
                tenantId,
                clientIp,
                requestUri,
                httpMethod,
                costMs,
                throwable == null ? null : throwable.getMessage(),
                Instant.now());
    }

    private SysLog resolveSysLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLog methodAnnotation = AnnotationUtils.findAnnotation(method, SysLog.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), SysLog.class);
    }

    private Long readTenantId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String tenantIdString = request.getParameter("tenantId");
        if (tenantIdString != null && !tenantIdString.isBlank()) {
            return Long.valueOf(tenantIdString.trim());
        }
        String headerTenantId = request.getHeader("X-Tenant-Id");
        return headerTenantId == null || headerTenantId.isBlank() ? null : Long.valueOf(headerTenantId.trim());
    }

    private String readRequestValue(HttpServletRequest request, String parameterName, String headerName) {
        if (request == null) {
            return null;
        }
        String value = request.getParameter(parameterName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return request.getHeader(headerName);
    }
}
