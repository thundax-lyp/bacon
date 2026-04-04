package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UserCredentialId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class UserCredentialIdTypeHandler extends AbstractLongBaseIdTypeHandler<UserCredentialId> {

    public UserCredentialIdTypeHandler() {
        super(UserCredentialId::of);
    }
}
