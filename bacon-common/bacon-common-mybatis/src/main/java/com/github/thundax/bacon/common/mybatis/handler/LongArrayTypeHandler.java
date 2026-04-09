package com.github.thundax.bacon.common.mybatis.handler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(Long[].class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class LongArrayTypeHandler extends AbstractJsonArrayTypeHandler<Long> {

    public LongArrayTypeHandler() {
        super(Long.class);
    }
}
