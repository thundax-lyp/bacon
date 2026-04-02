package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UserIdentityId.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class UserIdentityIdTypeHandler extends AbstractStringBaseIdTypeHandler<UserIdentityId> {

    public UserIdentityIdTypeHandler() {
        super(UserIdentityId::of);
    }
}
