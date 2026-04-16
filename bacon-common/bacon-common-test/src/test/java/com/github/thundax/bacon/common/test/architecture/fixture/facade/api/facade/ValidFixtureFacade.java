package com.github.thundax.bacon.common.test.architecture.fixture.facade.api.facade;

import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.request.ValidFixtureFacadeRequest;
import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.response.ValidFixtureFacadeResponse;

public interface ValidFixtureFacade {

    ValidFixtureFacadeResponse queryById(ValidFixtureFacadeRequest request);
}
