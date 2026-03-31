package com.github.thundax.bacon.upms.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleMenuRelDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMenuRelMapper extends BaseMapper<RoleMenuRelDO> {
}
