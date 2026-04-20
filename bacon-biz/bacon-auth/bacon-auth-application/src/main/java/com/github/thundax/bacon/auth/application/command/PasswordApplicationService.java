package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class PasswordApplicationService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final SessionApplicationService sessionApplicationService;
    private final UserPasswordFacade userPasswordFacade;

    public PasswordApplicationService(
            SessionApplicationService sessionApplicationService, UserPasswordFacade userPasswordFacade) {
        this.sessionApplicationService = sessionApplicationService;
        this.userPasswordFacade = userPasswordFacade;
    }

    public void changePassword(String accessToken, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new BadRequestException("Old password required");
        }
        if (newPassword == null || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new BadRequestException("New password invalid");
        }
        if (newPassword.equals(oldPassword)) {
            throw new BadRequestException("New password must differ from old password");
        }
        CurrentSessionDTO currentSession = sessionApplicationService.currentSession(accessToken);
        userPasswordFacade.changePassword(new UserPasswordChangeFacadeRequest(oldPassword, newPassword));
        sessionApplicationService.invalidateUserSessions(
                currentSession.getTenantId(), currentSession.getUserId(), "SELF_PASSWORD_CHANGED");
    }
}
