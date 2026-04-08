package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryOAuthClientRepositoryImpl implements OAuthClientRepository {

    private final TestAuthMemoryStore authStore;

    public InMemoryOAuthClientRepositoryImpl(TestAuthMemoryStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public Optional<OAuthClient> findByClientCode(String clientId) {
        return Optional.ofNullable(authStore.getClients().get(clientId));
    }
}
