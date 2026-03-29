package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "bacon.auth.repository.mode", havingValue = "memory")
public class InMemoryOAuthClientRepositoryImpl implements OAuthClientRepository {

    private final TestAuthMemoryStore authStore;

    public InMemoryOAuthClientRepositoryImpl(TestAuthMemoryStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        return Optional.ofNullable(authStore.getClients().get(clientId));
    }
}
