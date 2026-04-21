package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.upms.api.facade.UserPasswordFacade;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.application.command.UserPasswordApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class UserPasswordFacadeLocalImpl implements UserPasswordFacade {

    private final UserPasswordApplicationService userPasswordApplicationService;

    public UserPasswordFacadeLocalImpl(UserPasswordApplicationService userPasswordApplicationService) {
        this.userPasswordApplicationService = userPasswordApplicationService;
    }

    @Override
    public void changePassword(UserPasswordChangeFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        userPasswordApplicationService.changePassword(
                UserInterfaceAssembler.toPasswordChangeCommand(BaconIdContextHelper.requireUserId(), request));
    }
}
