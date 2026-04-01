package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UserCredentialId.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class UserCredentialIdTypeHandler extends AbstractStringBaseIdTypeHandler<UserCredentialId> {

    public UserCredentialIdTypeHandler() {
        super(UserCredentialId::of);
    }
}
