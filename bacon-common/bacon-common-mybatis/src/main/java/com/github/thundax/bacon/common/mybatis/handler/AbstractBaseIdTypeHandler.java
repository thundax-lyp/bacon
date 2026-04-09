package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.core.Identifier;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

abstract class AbstractBaseIdTypeHandler<I extends Identifier<T>, T> extends BaseTypeHandler<I> {

    private final Function<T, I> factory;

    protected AbstractBaseIdTypeHandler(Function<T, I> factory) {
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

    protected abstract T castValue(Object value) throws SQLException;

    private I toId(Object value) throws SQLException {
        T castedValue = castValue(value);
        return castedValue == null ? null : factory.apply(castedValue);
    }
}
