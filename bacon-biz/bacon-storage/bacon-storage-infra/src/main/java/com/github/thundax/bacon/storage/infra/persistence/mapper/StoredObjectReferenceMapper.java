package com.github.thundax.bacon.storage.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectReferenceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoredObjectReferenceMapper extends BaseMapper<StoredObjectReferenceDO> {}
