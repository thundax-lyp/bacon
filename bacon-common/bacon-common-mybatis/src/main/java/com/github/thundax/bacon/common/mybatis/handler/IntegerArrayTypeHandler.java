package com.github.thundax.bacon.common.mybatis.handler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(Integer[].class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class IntegerArrayTypeHandler extends AbstractJsonArrayTypeHandler<Integer> {

    public IntegerArrayTypeHandler() {
        super(Integer.class);
    }
}
