package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.request.RoleGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.RoleListByUserFacadeRequest;
import com.github.thundax.bacon.upms.api.response.RoleFacadeResponse;
import com.github.thundax.bacon.upms.api.response.RoleListFacadeResponse;

public interface RoleReadFacade {

    RoleFacadeResponse getRoleById(RoleGetFacadeRequest request);

    RoleListFacadeResponse getRolesByUserId(RoleListByUserFacadeRequest request);
}
