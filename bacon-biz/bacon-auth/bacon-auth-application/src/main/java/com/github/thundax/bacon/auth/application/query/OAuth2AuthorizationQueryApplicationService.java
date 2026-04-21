package com.github.thundax.bacon.auth.application.query;

import com.github.thundax.bacon.auth.application.assembler.OAuth2AuthorizationAssembler;
import com.github.thundax.bacon.auth.application.dto.OAuth2IntrospectionDTO;
import com.github.thundax.bacon.auth.application.dto.OAuth2UserinfoDTO;
import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthorizationQueryApplicationService {

    private final OAuthAuthorizationRepository oAuthAuthorizationRepository;
    private final OAuthClientRepository oAuthClientRepository;
    private final TokenCodec tokenCodec;
    private final PasswordEncoder passwordEncoder;

    public OAuth2AuthorizationQueryApplicationService(
            OAuthAuthorizationRepository oAuthAuthorizationRepository,
            OAuthClientRepository oAuthClientRepository,
            TokenCodec tokenCodec,
            PasswordEncoder passwordEncoder) {
        this.oAuthAuthorizationRepository = oAuthAuthorizationRepository;
        this.oAuthClientRepository = oAuthClientRepository;
        this.tokenCodec = tokenCodec;
        this.passwordEncoder = passwordEncoder;
    }

    public OAuth2IntrospectionDTO introspect(OAuth2IntrospectionQuery query) {
        validateClient(query.clientId(), query.clientSecret());
        return oAuthAuthorizationRepository
                .findAccessByHash(tokenCodec.sha256(query.token()))
                .filter(OAuthAccessToken::isActive)
                .filter(accessToken -> accessToken.getExpireAt().isAfter(Instant.now()))
                .map(accessToken -> OAuth2AuthorizationAssembler.toIntrospectionDto(
                        true,
                        accessToken.getClientIdValue(),
                        String.join(" ", accessToken.getScopes()),
                        String.valueOf(
                                accessToken.getUserId() == null
                                        ? null
                                        : accessToken.getUserId().value()),
                        accessToken.getTenantIdValue(),
                        accessToken.getExpireAt().getEpochSecond()))
                .orElse(OAuth2AuthorizationAssembler.toIntrospectionDto(false, query.clientId(), "", "", null, 0L));
    }

    public OAuth2UserinfoDTO userinfo(OAuth2UserinfoQuery query) {
        OAuthAccessToken token = oAuthAuthorizationRepository
                .findAccessByHash(tokenCodec.sha256(query.accessToken()))
                .filter(OAuthAccessToken::isActive)
                .orElseThrow(() -> new BadRequestException("OAuth access token invalid"));
        Long userId = token.getUserId() == null ? null : token.getUserId().value();
        String name = token.getScopes().contains("profile") ? "demo-user-" + userId : null;
        return OAuth2AuthorizationAssembler.toUserinfoDto(String.valueOf(userId), token.getTenantIdValue(), name);
    }

    private OAuthClient validateClient(String clientId, String clientSecret) {
        OAuthClient client = oAuthClientRepository
                .findByClientCode(clientId)
                .filter(OAuthClient::isEnabled)
                .orElseThrow(() -> new BadRequestException("OAuth client secret invalid"));
        String storedSecret = client.getClientSecret();
        boolean plainSecretMatches = storedSecret.equals(clientSecret);
        boolean hashedSecretMatches = passwordEncoder.matches(clientSecret, storedSecret);
        if (!plainSecretMatches && !hashedSecretMatches) {
            throw new BadRequestException("OAuth client secret invalid");
        }
        return client;
    }
}
