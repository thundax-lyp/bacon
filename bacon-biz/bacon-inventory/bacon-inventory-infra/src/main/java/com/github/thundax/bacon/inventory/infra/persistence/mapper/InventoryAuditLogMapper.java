package com.github.thundax.bacon.inventory.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDataObject;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryAuditLogMapper extends BaseMapper<InventoryAuditLogDataObject> {
}
