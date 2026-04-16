package com.github.thundax.bacon.common.test.architecture.fixture.layered.api.facade;

import com.github.thundax.bacon.common.test.architecture.fixture.layered.domain.model.valueobject.InvalidFixtureId;

public interface InvalidDomainDependentFacade {

    InvalidFixtureId getById(InvalidFixtureId id);
}
