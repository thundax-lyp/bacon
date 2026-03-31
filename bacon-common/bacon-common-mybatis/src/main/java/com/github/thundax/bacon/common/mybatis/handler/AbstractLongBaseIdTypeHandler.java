package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.core.Identifier;

import java.sql.SQLException;
import java.util.function.Function;

abstract class AbstractLongBaseIdTypeHandler<I extends Identifier<Long>> extends AbstractBaseIdTypeHandler<I, Long> {

    protected AbstractLongBaseIdTypeHandler(Function<Long, I> factory) {
        super(factory);
    }

    @Override
    protected Long castValue(Object value) throws SQLException {
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
