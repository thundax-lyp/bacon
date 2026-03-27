package com.github.thundax.bacon.payment.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentAuditLogMapper extends BaseMapper<PaymentAuditLogDO> {
}
