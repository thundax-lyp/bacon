package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Set;

public interface RoleResourceRepository {

    Set<ResourceCode> findResourceCodes(RoleId roleId);

    Set<ResourceCode> updateResourceCodes(RoleId roleId, Set<ResourceCode> resourceCodes);
}
