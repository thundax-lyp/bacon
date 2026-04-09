package com.github.thundax.bacon.upms.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCredentialMapper extends BaseMapper<UserCredentialDO> {}
