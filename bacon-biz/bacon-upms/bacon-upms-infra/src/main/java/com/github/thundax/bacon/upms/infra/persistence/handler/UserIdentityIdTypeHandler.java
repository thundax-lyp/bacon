package com.github.thundax.bacon.upms.infra.persistence.handler;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UserIdentityId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class UserIdentityIdTypeHandler extends AbstractLongUpmsIdTypeHandler<UserIdentityId> {

    public UserIdentityIdTypeHandler() {
        super(UserIdentityId::of);
    }
}
