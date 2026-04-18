package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.request.UserPermissionGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserMenuTreeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserPermissionCodeFacadeResponse;

public interface PermissionReadFacade {

    UserMenuTreeFacadeResponse listMenuTreeByUserId(UserPermissionGetFacadeRequest request);

    UserPermissionCodeFacadeResponse findPermissionCodesByUserId(UserPermissionGetFacadeRequest request);

    UserDataScopeFacadeResponse getUserDataScope(UserPermissionGetFacadeRequest request);
}
