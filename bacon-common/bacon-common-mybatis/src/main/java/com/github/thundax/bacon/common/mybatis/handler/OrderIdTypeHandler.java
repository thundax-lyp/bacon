package com.github.thundax.bacon.common.mybatis.handler;

import com.github.thundax.bacon.common.id.domain.OrderId;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(OrderId.class)
@MappedJdbcTypes({JdbcType.BIGINT, JdbcType.INTEGER, JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class OrderIdTypeHandler extends AbstractStringBaseIdTypeHandler<OrderId> {

    public OrderIdTypeHandler() {
        super(OrderId::of);
    }
}
