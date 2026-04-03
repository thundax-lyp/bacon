package com.github.thundax.bacon.order.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxDeadLetterDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderOutboxDeadLetterMapper extends BaseMapper<OrderOutboxDeadLetterDO> {
}
