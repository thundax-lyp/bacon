package com.github.thundax.bacon.product.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSpuDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductSpuMapper extends BaseMapper<ProductSpuDO> {}
