package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.RoleId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(RoleId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class RoleIdTypeHandler extends AbstractStringBaseIdTypeHandler<RoleId> {

    public RoleIdTypeHandler() {
        super(RoleId::of);
    }

    @Override
    protected String castValue(Object value) throws java.sql.SQLException {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof Number number) {
            return String.valueOf(number.longValue());
        }
        throw new java.sql.SQLException("Unsupported role id value type: " + value.getClass().getName());
    }
}
