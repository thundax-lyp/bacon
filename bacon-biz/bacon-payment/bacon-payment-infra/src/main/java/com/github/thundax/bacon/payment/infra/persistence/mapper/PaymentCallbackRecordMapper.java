package com.github.thundax.bacon.payment.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentCallbackRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentCallbackRecordMapper extends BaseMapper<PaymentCallbackRecordDO> {
}
