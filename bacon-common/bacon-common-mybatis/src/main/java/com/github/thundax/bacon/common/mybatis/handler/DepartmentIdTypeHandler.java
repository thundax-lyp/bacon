package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(DepartmentId.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class DepartmentIdTypeHandler extends AbstractStringBaseIdTypeHandler<DepartmentId> {

    public DepartmentIdTypeHandler() {
        super(DepartmentId::of);
    }
}
