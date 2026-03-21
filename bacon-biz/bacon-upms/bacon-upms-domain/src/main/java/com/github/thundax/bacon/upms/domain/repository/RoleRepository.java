package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findRoleById(Long tenantId, Long roleId);

    List<Role> findRolesByUserId(Long tenantId, Long userId);
}
