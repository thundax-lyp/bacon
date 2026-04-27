package com.github.thundax.bacon.product.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductImageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImageDO> {}
