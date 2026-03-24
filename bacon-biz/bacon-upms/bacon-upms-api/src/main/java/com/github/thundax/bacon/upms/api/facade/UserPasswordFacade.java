package com.github.thundax.bacon.upms.api.facade;

public interface UserPasswordFacade {

    void changePassword(Long tenantId, Long userId, String oldPassword, String newPassword);
}
