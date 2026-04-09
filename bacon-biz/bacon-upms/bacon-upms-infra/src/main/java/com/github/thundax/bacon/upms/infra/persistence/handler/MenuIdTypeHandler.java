package com.github.thundax.bacon.upms.infra.persistence.handler;

import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(MenuId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class MenuIdTypeHandler extends AbstractLongUpmsIdTypeHandler<MenuId> {

    public MenuIdTypeHandler() {
        super(MenuId::of);
    }
}
