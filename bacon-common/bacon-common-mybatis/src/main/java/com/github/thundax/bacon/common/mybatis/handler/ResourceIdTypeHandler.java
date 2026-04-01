package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(ResourceId.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ResourceIdTypeHandler extends AbstractStringBaseIdTypeHandler<ResourceId> {

    public ResourceIdTypeHandler() {
        super(ResourceId::of);
    }
}
