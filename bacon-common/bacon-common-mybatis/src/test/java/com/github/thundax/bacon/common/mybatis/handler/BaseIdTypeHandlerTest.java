package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class BaseIdTypeHandlerTest {

    @Test
    void shouldWriteUnderlyingLongValue() throws Exception {
        UserIdTypeHandler handler = new UserIdTypeHandler();
        AtomicReference<Object> writtenValue = new AtomicReference<>();
        PreparedStatement preparedStatement = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("setObject".equals(method.getName())) {
                        writtenValue.set(args[1]);
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                });

        handler.setNonNullParameter(preparedStatement, 1, UserId.of(1001L), null);

        assertThat(writtenValue.get()).isEqualTo(1001L);
    }

    @Test
    void shouldReadLongIdentifierFromResultSet() throws Exception {
        UserIdTypeHandler handler = new UserIdTypeHandler();
        ResultSet resultSet = resultSet(Map.of("user_id", 1001L, 1, 1002L));
        CallableStatement callableStatement = callableStatement(Map.of(1, 1003L));

        assertThat(handler.getNullableResult(resultSet, "user_id")).isEqualTo(UserId.of(1001L));
        assertThat(handler.getNullableResult(resultSet, 1)).isEqualTo(UserId.of(1002L));
        assertThat(handler.getNullableResult(callableStatement, 1)).isEqualTo(UserId.of(1003L));
    }

    @Test
    void shouldReadTenantIdentifierFromResultSet() throws Exception {
        TenantIdTypeHandler handler = new TenantIdTypeHandler();
        ResultSet resultSet = resultSet(Map.of("tenant_id", 1001L));

        assertThat(handler.getNullableResult(resultSet, "tenant_id")).isEqualTo(TenantId.of(1001L));
    }

    @Test
    void shouldReadUserIdentityIdentifierFromResultSet() throws Exception {
        UserIdentityIdTypeHandler handler = new UserIdentityIdTypeHandler();
        ResultSet resultSet = resultSet(Map.of("identity_id", 1001L));

        assertThat(handler.getNullableResult(resultSet, "identity_id")).isEqualTo(UserIdentityId.of(1001L));
    }

    @Test
    void shouldReadNumericRoleIdentifierAsStringValue() throws Exception {
        RoleIdTypeHandler handler = new RoleIdTypeHandler();
        ResultSet resultSet = resultSet(Map.of("role_id", 1001L, 1, "1002"));

        assertThat(handler.getNullableResult(resultSet, "role_id")).isEqualTo(RoleId.of(1001L));
        assertThat(handler.getNullableResult(resultSet, 1)).isEqualTo(RoleId.of(1002L));
    }

    private ResultSet resultSet(Map<Object, Object> values) {
        Map<Object, Object> mutableValues = new HashMap<>(values);
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> {
                    if ("getObject".equals(method.getName())) {
                        return mutableValues.get(args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private CallableStatement callableStatement(Map<Integer, Object> values) {
        Map<Integer, Object> mutableValues = new HashMap<>(values);
        return (CallableStatement) Proxy.newProxyInstance(
                CallableStatement.class.getClassLoader(),
                new Class[]{CallableStatement.class},
                (proxy, method, args) -> {
                    if ("getObject".equals(method.getName())) {
                        return mutableValues.get(args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
