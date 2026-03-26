package com.github.thundax.bacon.inventory.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditDeadLetterDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryAuditDeadLetterMapper extends BaseMapper<InventoryAuditDeadLetterDO> {
}
