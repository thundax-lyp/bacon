package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findById(RoleId roleId);

    List<Role> findByUserId(UserId userId);

    List<Role> page(RoleCode code, String name, RoleType roleType, RoleStatus status, int pageNo, int pageSize);

    long count(RoleCode code, String name, RoleType roleType, RoleStatus status);

    Role insert(Role role);

    Role update(Role role);

    void delete(RoleId roleId);
}
