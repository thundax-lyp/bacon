package com.github.thundax.bacon.product.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductOutboxDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductOutboxMapper extends BaseMapper<ProductOutboxDO> {}
