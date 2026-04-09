package com.github.thundax.bacon.upms.infra.persistence.handler;

import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(DepartmentId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class DepartmentIdTypeHandler extends AbstractLongUpmsIdTypeHandler<DepartmentId> {

    public DepartmentIdTypeHandler() {
        super(DepartmentId::of);
    }
}
