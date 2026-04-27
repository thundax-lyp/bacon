package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.application.query.SessionCurrentQuery;
import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.auth.domain.exception.AuthDomainException;
import com.github.thundax.bacon.auth.domain.exception.AuthErrorCode;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class PasswordCommandApplicationService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final SessionQueryApplicationService sessionQueryApplicationService;
    private final SessionCommandApplicationService sessionCommandApplicationService;
    private final UserPasswordFacade userPasswordFacade;

    public PasswordCommandApplicationService(
            SessionQueryApplicationService sessionQueryApplicationService,
            SessionCommandApplicationService sessionCommandApplicationService,
            UserPasswordFacade userPasswordFacade) {
        this.sessionQueryApplicationService = sessionQueryApplicationService;
        this.sessionCommandApplicationService = sessionCommandApplicationService;
        this.userPasswordFacade = userPasswordFacade;
    }

    public void changePassword(PasswordChangeCommand command) {
        if (command.oldPassword() == null || command.oldPassword().isBlank()) {
            throw new AuthDomainException(AuthErrorCode.OLD_PASSWORD_REQUIRED);
        }
        if (command.newPassword() == null || !PASSWORD_PATTERN.matcher(command.newPassword()).matches()) {
            throw new AuthDomainException(AuthErrorCode.NEW_PASSWORD_INVALID);
        }
        if (command.newPassword().equals(command.oldPassword())) {
            throw new AuthDomainException(AuthErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }
        var currentSession = sessionQueryApplicationService.currentSession(new SessionCurrentQuery(command.accessToken()));
        userPasswordFacade.changePassword(new UserPasswordChangeFacadeRequest(command.oldPassword(), command.newPassword()));
        sessionCommandApplicationService.invalidateUserSessions(
                new SessionInvalidateUserCommand(currentSession.getTenantId(), currentSession.getUserId(), "SELF_PASSWORD_CHANGED"));
    }
}
