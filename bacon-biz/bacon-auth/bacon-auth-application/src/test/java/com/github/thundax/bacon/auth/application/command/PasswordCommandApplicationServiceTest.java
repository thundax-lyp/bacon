package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.query.SessionCurrentQuery;
import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordCommandApplicationServiceTest {

    @Mock
    private SessionQueryApplicationService sessionQueryApplicationService;
    @Mock
    private SessionCommandApplicationService sessionCommandApplicationService;
    @Mock
    private UserPasswordFacade userPasswordFacade;

    private PasswordCommandApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PasswordCommandApplicationService(
                sessionQueryApplicationService, sessionCommandApplicationService, userPasswordFacade);
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

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

    @Test
    void shouldChangePasswordAndInvalidateUserSessionsWhenCommandIsValid() {
        when(sessionQueryApplicationService.currentSession(new SessionCurrentQuery("access-token")))
                .thenReturn(new CurrentSessionDTO(
                        "session-1",
                        1001L,
                        2001L,
                        "ACCOUNT",
                        "PASSWORD",
                        "ACTIVE",
                        null,
                        null,
                        null));

        assertDoesNotThrow(() ->
                service.changePassword(new PasswordChangeCommand("access-token", "OldPass123", "NewPass123")));

        verify(userPasswordFacade).changePassword(argThat(request ->
                request.getOldPassword().equals("OldPass123")
                        && request.getNewPassword().equals("NewPass123")));
        verify(sessionCommandApplicationService).invalidateUserSessions(
                new SessionInvalidateUserCommand(1001L, 2001L, "SELF_PASSWORD_CHANGED"));
    }
}
