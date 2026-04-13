package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.common.id.domain.UserId;
import org.springframework.lang.NonNull;

public interface UserPasswordFacade {

    void changePassword(@NonNull UserId userId, String oldPassword, String newPassword);
}
