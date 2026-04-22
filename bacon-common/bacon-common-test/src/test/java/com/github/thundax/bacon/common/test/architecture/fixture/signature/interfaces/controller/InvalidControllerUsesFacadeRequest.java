package com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.controller;

import com.github.thundax.bacon.common.test.architecture.fixture.signature.api.request.InvalidFixtureFacadeRequest;
import com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.response.ValidFixtureResponse;

public class InvalidControllerUsesFacadeRequest {

    public ValidFixtureResponse create(InvalidFixtureFacadeRequest request) {
        return new ValidFixtureResponse(request.id());
    }
}
