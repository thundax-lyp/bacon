package com.github.thundax.bacon.common.test.architecture.fixture.layered.api.facade;

import com.github.thundax.bacon.common.test.architecture.fixture.layered.api.request.ValidFixtureRequest;

public interface ValidFixtureFacade {

    void handle(ValidFixtureRequest request);
}
