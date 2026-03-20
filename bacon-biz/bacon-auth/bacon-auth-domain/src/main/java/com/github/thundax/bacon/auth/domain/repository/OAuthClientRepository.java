package com.github.thundax.bacon.auth.domain.repository;

import com.github.thundax.bacon.auth.domain.entity.OAuthClient;
import java.util.Optional;

public interface OAuthClientRepository {

    Optional<OAuthClient> findByClientId(String clientId);
}
