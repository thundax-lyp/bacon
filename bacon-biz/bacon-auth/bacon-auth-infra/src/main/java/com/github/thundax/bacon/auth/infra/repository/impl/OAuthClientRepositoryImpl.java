package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OAuthClientRepositoryImpl implements OAuthClientRepository {

    private final InMemoryAuthStore authStore;

    public OAuthClientRepositoryImpl(InMemoryAuthStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        return Optional.ofNullable(authStore.getClients().get(clientId));
    }
}
