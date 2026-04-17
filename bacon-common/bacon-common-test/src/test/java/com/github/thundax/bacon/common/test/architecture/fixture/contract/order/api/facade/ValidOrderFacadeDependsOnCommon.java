package com.github.thundax.bacon.common.test.architecture.fixture.contract.order.api.facade;

import com.github.thundax.bacon.common.test.architecture.fixture.contract.common.support.CommonSupport;

public interface ValidOrderFacadeDependsOnCommon {

    default String normalize(String orderNo) {
        return CommonSupport.normalize(orderNo);
    }
}
