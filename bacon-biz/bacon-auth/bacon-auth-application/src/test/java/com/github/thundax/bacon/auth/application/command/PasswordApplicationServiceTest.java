package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import org.junit.jupiter.api.Test;

class PasswordApplicationServiceTest {

    private final SessionApplicationService sessionApplicationService = mock(SessionApplicationService.class);
    private final UserPasswordFacade userPasswordFacade = mock(UserPasswordFacade.class);
    private final PasswordApplicationService service =
            new PasswordApplicationService(sessionApplicationService, userPasswordFacade);

    @Test
    void shouldThrowBadRequestWhenOldPasswordIsBlank() {
        assertThrows(BadRequestException.class, () -> service.changePassword("access-token", " ", "NewPass123"));
    }

    @Test
    void shouldThrowBadRequestWhenNewPasswordIsInvalid() {
        assertThrows(BadRequestException.class, () -> service.changePassword("access-token", "OldPass123", "short"));
    }

    @Test
    void shouldThrowBadRequestWhenNewPasswordMatchesOldPassword() {
        assertThrows(
                BadRequestException.class,
                () -> service.changePassword("access-token", "SamePass123", "SamePass123"));
    }
}
