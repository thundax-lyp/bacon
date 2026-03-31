package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordApplicationService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final SessionApplicationService sessionApplicationService;
    private final UserPasswordFacade userPasswordFacade;

    public PasswordApplicationService(SessionApplicationService sessionApplicationService,
                                      UserPasswordFacade userPasswordFacade) {
        this.sessionApplicationService = sessionApplicationService;
        this.userPasswordFacade = userPasswordFacade;
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
        CurrentSessionDTO currentSession = sessionApplicationService.currentSession(accessToken);
        userPasswordFacade.changePassword(currentSession.getTenantNo(), currentSession.getUserId(),
                oldPassword, newPassword);
        sessionApplicationService.invalidateUserSessions(currentSession.getTenantNo(), currentSession.getUserId(),
                "SELF_PASSWORD_CHANGED");
    }
}
