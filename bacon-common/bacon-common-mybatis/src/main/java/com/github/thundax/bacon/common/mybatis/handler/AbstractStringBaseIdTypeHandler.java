package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.core.Identifier;

import java.sql.SQLException;
import java.util.function.Function;

abstract class AbstractStringBaseIdTypeHandler<I extends Identifier<String>> extends AbstractBaseIdTypeHandler<I, String> {

    protected AbstractStringBaseIdTypeHandler(Function<String, I> factory) {
        super(factory);
    }

    @Override
    protected String castValue(Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new SQLException("Unsupported string id value type: " + value.getClass().getName());
    }
}
