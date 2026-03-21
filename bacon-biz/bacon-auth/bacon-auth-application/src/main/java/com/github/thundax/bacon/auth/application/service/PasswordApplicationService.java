package com.github.thundax.bacon.auth.application.service;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordApplicationService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final SessionApplicationService sessionApplicationService;

    public PasswordApplicationService(SessionApplicationService sessionApplicationService) {
        this.sessionApplicationService = sessionApplicationService;
    }

    public void changePassword(String accessToken, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password required");
        }
        if (newPassword == null || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalArgumentException("New password invalid");
        }
        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("New password must differ from old password");
        }
        CurrentSessionResponse currentSession = sessionApplicationService.currentSession(accessToken);
        sessionApplicationService.invalidateUserSessions(currentSession.getTenantId(), currentSession.getUserId(),
                "SELF_PASSWORD_CHANGED");
    }
}
