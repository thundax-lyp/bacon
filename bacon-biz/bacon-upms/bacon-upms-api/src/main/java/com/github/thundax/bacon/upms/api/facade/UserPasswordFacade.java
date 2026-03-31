package com.github.thundax.bacon.upms.api.facade;

public interface UserPasswordFacade {

    void changePassword(String tenantId, String userId, String oldPassword, String newPassword);
}
