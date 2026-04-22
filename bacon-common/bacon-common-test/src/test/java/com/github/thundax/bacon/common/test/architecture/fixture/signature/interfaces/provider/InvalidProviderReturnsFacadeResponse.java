package com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.provider;

import com.github.thundax.bacon.common.test.architecture.fixture.signature.api.response.InvalidFixtureFacadeResponse;

public class InvalidProviderReturnsFacadeResponse {

    public InvalidFixtureFacadeResponse getById(String id) {
        return new InvalidFixtureFacadeResponse(id);
    }
}
