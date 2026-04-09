package com.github.thundax.bacon.auth.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.model.enums.ClientStatus;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import com.github.thundax.bacon.auth.infra.persistence.dataobject.OAuthClientDO;
import com.github.thundax.bacon.auth.infra.persistence.mapper.OAuthClientMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OAuthClientRepositoryImpl implements OAuthClientRepository {

    private static final TypeReference<LinkedHashSet<String>> STRING_SET_TYPE = new TypeReference<>() {};

    private final OAuthClientMapper oAuthClientMapper;
    private final ObjectMapper objectMapper;

    public OAuthClientRepositoryImpl(OAuthClientMapper oAuthClientMapper, ObjectMapper objectMapper) {
        this.oAuthClientMapper = oAuthClientMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<OAuthClient> findByClientCode(String clientId) {
        return Optional.ofNullable(oAuthClientMapper.selectOne(Wrappers.<OAuthClientDO>lambdaQuery()
                        .eq(OAuthClientDO::getClientId, clientId)
                        .last("limit 1")))
                .map(this::toDomain);
    }

    private OAuthClient toDomain(OAuthClientDO dataObject) {
        return new OAuthClient(
                dataObject.getId(),
                dataObject.getClientId(),
                dataObject.getClientSecretHash(),
                dataObject.getClientName(),
                dataObject.getClientType(),
                readStringSet(dataObject.getGrantTypes()).stream().toList(),
                readStringSet(dataObject.getScopes()).stream().toList(),
                readStringSet(dataObject.getRedirectUris()).stream().toList(),
                dataObject.getAccessTokenTtlSeconds(),
                dataObject.getRefreshTokenTtlSeconds(),
                Boolean.TRUE.equals(dataObject.getEnabled()) ? ClientStatus.ENABLED : ClientStatus.DISABLED,
                dataObject.getContact(),
                dataObject.getRemark(),
                defaultInstant(dataObject.getCreatedAt()),
                defaultInstant(dataObject.getUpdatedAt()));
    }

    private Set<String> readStringSet(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashSet<>();
        }
        try {
            return objectMapper.readValue(json, STRING_SET_TYPE);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to deserialize oauth client set field", ex);
        }
    }

    private Instant defaultInstant(Instant value) {
        return value == null ? Instant.EPOCH : value;
    }
}
