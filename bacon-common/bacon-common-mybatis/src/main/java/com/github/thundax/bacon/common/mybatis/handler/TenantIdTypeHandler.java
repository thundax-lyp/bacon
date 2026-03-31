package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.TenantId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(TenantId.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class TenantIdTypeHandler extends AbstractStringBaseIdTypeHandler<TenantId> {

    public TenantIdTypeHandler() {
        super(TenantId::of);
    }
}
