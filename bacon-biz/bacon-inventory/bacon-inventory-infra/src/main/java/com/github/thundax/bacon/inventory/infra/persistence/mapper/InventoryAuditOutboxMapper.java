package com.github.thundax.bacon.inventory.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditOutboxDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryAuditOutboxMapper extends BaseMapper<InventoryAuditOutboxDO> {}
