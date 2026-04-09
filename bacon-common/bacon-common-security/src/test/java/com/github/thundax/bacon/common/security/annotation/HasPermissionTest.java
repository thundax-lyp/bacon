package com.github.thundax.bacon.common.security.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;

class HasPermissionTest {

    @Test
    void shouldResolvePermissionCodeIntoPreAuthorizeExpression() throws NoSuchMethodException {
        Method method = SecuredController.class.getDeclaredMethod("list");
        AnnotationTemplateExpressionDefaults defaults = new AnnotationTemplateExpressionDefaults();
        PreAuthorize preAuthorize = SecurityAnnotationScanners.requireUnique(PreAuthorize.class, defaults)
                .scan(method, SecuredController.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('sys:user:view')");
    }

    static class SecuredController {

        @HasPermission("sys:user:view")
        void list() {}
    }
}
