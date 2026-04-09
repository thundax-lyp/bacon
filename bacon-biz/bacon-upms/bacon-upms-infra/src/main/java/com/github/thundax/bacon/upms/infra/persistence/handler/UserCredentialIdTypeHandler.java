package com.github.thundax.bacon.upms.infra.persistence.handler;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UserCredentialId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class UserCredentialIdTypeHandler extends AbstractLongUpmsIdTypeHandler<UserCredentialId> {

    public UserCredentialIdTypeHandler() {
        super(UserCredentialId::of);
    }
}
