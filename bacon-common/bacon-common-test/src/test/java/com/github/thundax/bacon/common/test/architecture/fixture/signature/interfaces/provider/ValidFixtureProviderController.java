package com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.provider;

import com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.request.ValidFixtureRequest;
import com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.response.ValidFixtureResponse;
import java.util.Set;

public class ValidFixtureProviderController {

    public ValidFixtureResponse create(ValidFixtureRequest request) {
        return new ValidFixtureResponse(request.id());
    }

    public Set<ValidFixtureResponse> listByKeyword(String keyword) {
        return Set.of(new ValidFixtureResponse(keyword));
    }

    public void delete(String id) {}
}
