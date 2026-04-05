package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import com.github.thundax.bacon.upms.application.command.UserApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class UserPasswordFacadeLocalImpl implements UserPasswordFacade {

    private final UserApplicationService userApplicationService;

    public UserPasswordFacadeLocalImpl(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Override
    public void changePassword(@NonNull TenantId tenantId, @NonNull UserId userId, String oldPassword, String newPassword) {
        userApplicationService.changePassword(tenantId, userId, oldPassword, newPassword);
    }
}
