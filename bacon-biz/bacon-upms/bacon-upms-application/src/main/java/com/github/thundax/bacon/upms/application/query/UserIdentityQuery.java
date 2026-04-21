package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;

public record UserIdentityQuery(UserIdentityType identityType, String identityValue) {}
