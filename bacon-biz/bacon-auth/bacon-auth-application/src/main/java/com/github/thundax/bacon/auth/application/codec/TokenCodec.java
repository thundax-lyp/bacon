package com.github.thundax.bacon.auth.application.codec;

import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
public class TokenCodec {

    public String issueUserAccessToken(AuthSession authSession) {
        String payload = authSession.getSessionId() + ":" + authSession.getExpireAt().getEpochSecond();
        return "bacon-user." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<String> parseSessionId(String accessToken) {
        if (accessToken == null || !accessToken.startsWith("bacon-user.")) {
            return Optional.empty();
        }
        String encodedPayload = accessToken.substring("bacon-user.".length());
        String decoded = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":");
        if (parts.length != 2) {
            return Optional.empty();
        }
        long expireAt = Long.parseLong(parts[1]);
        if (Instant.ofEpochSecond(expireAt).isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(parts[0]);
    }

    public String randomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
