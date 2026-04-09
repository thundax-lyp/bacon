package com.github.thundax.bacon.storage.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StorageAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StorageAuditLogMapper extends BaseMapper<StorageAuditLogDO> {}
