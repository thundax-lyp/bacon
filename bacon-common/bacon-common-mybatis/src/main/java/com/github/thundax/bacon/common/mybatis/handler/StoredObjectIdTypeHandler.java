package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(StoredObjectId.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class StoredObjectIdTypeHandler extends AbstractStringBaseIdTypeHandler<StoredObjectId> {

    public StoredObjectIdTypeHandler() {
        super(StoredObjectId::of);
    }
}
