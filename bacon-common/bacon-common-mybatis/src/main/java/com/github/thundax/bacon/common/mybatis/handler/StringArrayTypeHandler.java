package com.github.thundax.bacon.common.mybatis.handler;

import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.JdbcType;

@MappedTypes(String[].class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class StringArrayTypeHandler extends AbstractJsonArrayTypeHandler<String> {

    public StringArrayTypeHandler() {
        super(String.class);
    }
}
