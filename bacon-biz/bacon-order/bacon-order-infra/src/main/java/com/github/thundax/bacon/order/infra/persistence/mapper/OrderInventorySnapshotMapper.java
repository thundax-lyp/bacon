package com.github.thundax.bacon.order.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDataObject;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderInventorySnapshotMapper extends BaseMapper<OrderInventorySnapshotDataObject> {
}
