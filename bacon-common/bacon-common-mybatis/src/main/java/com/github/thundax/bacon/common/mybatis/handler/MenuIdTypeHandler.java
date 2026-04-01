package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.MenuId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(MenuId.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class MenuIdTypeHandler extends AbstractStringBaseIdTypeHandler<MenuId> {

    public MenuIdTypeHandler() {
        super(MenuId::of);
    }
}
