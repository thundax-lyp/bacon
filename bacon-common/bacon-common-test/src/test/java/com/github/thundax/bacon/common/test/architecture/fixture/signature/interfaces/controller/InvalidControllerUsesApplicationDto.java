package com.github.thundax.bacon.common.test.architecture.fixture.signature.interfaces.controller;

import com.github.thundax.bacon.common.test.architecture.fixture.signature.application.dto.InvalidFixtureDTO;

public class InvalidControllerUsesApplicationDto {

    public InvalidFixtureDTO getById(String id) {
        return new InvalidFixtureDTO(id);
    }
}
