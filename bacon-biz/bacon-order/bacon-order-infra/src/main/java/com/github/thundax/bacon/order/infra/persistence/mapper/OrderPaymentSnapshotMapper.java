package com.github.thundax.bacon.order.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderPaymentSnapshotMapper extends BaseMapper<OrderPaymentSnapshotDO> {
}
