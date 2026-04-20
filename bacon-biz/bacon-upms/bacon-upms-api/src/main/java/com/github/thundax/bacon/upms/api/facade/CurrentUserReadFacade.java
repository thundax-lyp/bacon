package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;

public interface CurrentUserReadFacade {

    UserFacadeResponse getCurrentUser();

    TenantFacadeResponse getCurrentTenant();

    UserDataScopeFacadeResponse getCurrentDataScope();
}
