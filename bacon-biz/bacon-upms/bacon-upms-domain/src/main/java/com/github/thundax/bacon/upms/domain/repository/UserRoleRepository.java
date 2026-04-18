package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;

public interface UserRoleRepository {

    List<Role> updateRoleIds(UserId userId, List<RoleId> roleIds);
}
