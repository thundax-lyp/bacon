package com.github.thundax.bacon.common.mybatis.handler;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

abstract class AbstractJsonArrayTypeHandler<T> extends BaseTypeHandler<T[]> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Class<T> elementType;

    protected AbstractJsonArrayTypeHandler(Class<T> elementType) {
        this.elementType = elementType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T[] parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (Exception ex) {
            throw new SQLException("Failed to serialize array parameter", ex);
        }
    }

    @Override
    public T[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseArray(rs.getString(columnName));
    }

    @Override
    public T[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseArray(rs.getString(columnIndex));
    }

    @Override
    public T[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseArray(cs.getString(columnIndex));
    }

    private T[] parseArray(String content) throws SQLException {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructArrayType(elementType);
            return OBJECT_MAPPER.readValue(content, javaType);
        } catch (Exception ex) {
            throw new SQLException("Failed to deserialize array value", ex);
        }
    }
}
