package com.github.thundax.bacon.product.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductSnapshotMapper extends BaseMapper<ProductSnapshotDO> {}
