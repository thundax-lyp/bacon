package com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.controller;

import com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.request.ValidFixtureRequest;
import com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.response.ValidFixtureResponse;
import java.util.List;

public class ValidFixtureController {

    public ValidFixtureResponse create(ValidFixtureRequest request) {
        return new ValidFixtureResponse(request.id());
    }

    public List<ValidFixtureResponse> listByKeyword(String keyword) {
        return List.of(new ValidFixtureResponse(keyword));
    }

    public ValidFixtureResponse getById(String id) {
        return new ValidFixtureResponse(id);
    }

    public void delete(String id) {}
}
