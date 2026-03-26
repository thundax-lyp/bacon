package com.github.thundax.bacon.auth.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Getter
@AllArgsConstructor
public class OAuthClient {

    private Long id;
    private String clientId;
    private String clientSecret;
    private String clientName;
    private String clientType;
    private Set<String> grantTypes;
    private Set<String> scopes;
    private Set<String> redirectUris;
    private long accessTokenTtlSeconds;
    private long refreshTokenTtlSeconds;
    private boolean enabled;
    private String contact;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;
}
