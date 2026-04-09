package com.github.thundax.bacon.upms.infra.persistence.handler;

import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(PostId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class PostIdTypeHandler extends AbstractLongUpmsIdTypeHandler<PostId> {

    public PostIdTypeHandler() {
        super(PostId::of);
    }
}
