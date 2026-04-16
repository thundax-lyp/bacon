package com.github.thundax.bacon.common.test.architecture.fixture.facade.api.facade;

import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.dto.InvalidFixtureDTO;
import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.request.ValidFixtureFacadeRequest;

public interface InvalidFixtureFacade {

    InvalidFixtureDTO queryById(ValidFixtureFacadeRequest request);

    InvalidFixtureDTO listByKeyword(String keyword);
}
