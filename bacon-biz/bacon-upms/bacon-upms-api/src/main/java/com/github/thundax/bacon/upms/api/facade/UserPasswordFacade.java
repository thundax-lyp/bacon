package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;

public interface UserPasswordFacade {

    void changePassword(UserPasswordChangeFacadeRequest request);
}
