package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import org.junit.jupiter.api.Test;

class PasswordCommandApplicationServiceTest {

    private final SessionQueryApplicationService sessionQueryApplicationService = mock(SessionQueryApplicationService.class);
    private final SessionCommandApplicationService sessionCommandApplicationService =
            mock(SessionCommandApplicationService.class);
    private final UserPasswordFacade userPasswordFacade = mock(UserPasswordFacade.class);
    private final PasswordCommandApplicationService service = new PasswordCommandApplicationService(
            sessionQueryApplicationService, sessionCommandApplicationService, userPasswordFacade);

    @Test
    void shouldThrowBadRequestWhenOldPasswordIsBlank() {
        assertThrows(
                BadRequestException.class,
                () -> service.changePassword(new PasswordChangeCommand("access-token", " ", "NewPass123")));
    }

    @Test
    void shouldThrowBadRequestWhenNewPasswordIsInvalid() {
        assertThrows(
                BadRequestException.class,
                () -> service.changePassword(new PasswordChangeCommand("access-token", "OldPass123", "short")));
    }

    @Test
    void shouldThrowBadRequestWhenNewPasswordMatchesOldPassword() {
        assertThrows(
                BadRequestException.class,
                () -> service.changePassword(new PasswordChangeCommand("access-token", "SamePass123", "SamePass123")));
    }
}
