package com.github.thundax.bacon.upms.api.facade;

public interface UserPasswordFacade {

    void changePassword(String tenantNo, String userId, String oldPassword, String newPassword);
}
