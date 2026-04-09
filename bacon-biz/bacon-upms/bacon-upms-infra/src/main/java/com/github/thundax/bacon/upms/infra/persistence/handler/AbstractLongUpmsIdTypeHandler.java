package com.github.thundax.bacon.upms.infra.persistence.handler;

import com.github.thundax.bacon.common.id.core.Identifier;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

abstract class AbstractLongUpmsIdTypeHandler<I extends Identifier<Long>> extends BaseTypeHandler<I> {

    private final Function<Long, I> factory;

    protected AbstractLongUpmsIdTypeHandler(Function<Long, I> factory) {
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, I parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.value());
    }

    @Override
    public I getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toId(rs.getObject(columnName));
    }

    @Override
    public I getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toId(rs.getObject(columnIndex));
    }

    @Override
    public I getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toId(cs.getObject(columnIndex));
    }

    private I toId(Object value) throws SQLException {
        Long castedValue = castValue(value);
        return castedValue == null ? null : factory.apply(castedValue);
    }

    private Long castValue(Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ex) {
                throw new SQLException("Failed to parse long id value: " + stringValue, ex);
            }
        }
        throw new SQLException("Unsupported long id value type: " + value.getClass().getName());
    }
}
