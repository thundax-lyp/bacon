package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.application.assembler.RoleAssembler;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.codec.ResourceCodeCodec;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleDataScopeRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleMenuRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleResourceRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RoleQueryApplicationService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final RoleDataScopeRepository roleDataScopeRepository;

    public RoleQueryApplicationService(
            RoleRepository roleRepository,
            RoleMenuRepository roleMenuRepository,
            RoleResourceRepository roleResourceRepository,
            RoleDataScopeRepository roleDataScopeRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleResourceRepository = roleResourceRepository;
        this.roleDataScopeRepository = roleDataScopeRepository;
    }

    public RoleDTO getById(RoleId roleId) {
        return RoleAssembler.toDto(roleRepository
                .findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value())));
    }

    public List<RoleDTO> getByUserId(UserId userId) {
        return roleRepository.findByUserId(userId).stream()
                .map(RoleAssembler::toDto)
                .toList();
    }

    public PageResult<RoleDTO> page(RolePageQuery query) {
        int normalizedPageNo = query.getPageNo();
        int normalizedPageSize = query.getPageSize();
        return new PageResult<>(
                roleRepository
                        .page(
                                query.getCode(),
                                query.getName(),
                                query.getRoleType(),
                                query.getStatus(),
                                normalizedPageNo,
                                normalizedPageSize)
                        .stream()
                        .map(RoleAssembler::toDto)
                        .toList(),
                roleRepository.count(query.getCode(), query.getName(), query.getRoleType(), query.getStatus()),
                normalizedPageNo,
                normalizedPageSize);
    }

    public Set<String> getMenuIds(RoleId roleId) {
        return roleMenuRepository.findMenuIds(roleId).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getResourceCodes(RoleId roleId) {
        return roleResourceRepository.findResourceCodes(roleId).stream()
                .map(ResourceCodeCodec::toValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public RoleDataScopeType getDataScopeType(RoleId roleId) {
        return roleDataScopeRepository.findDataScopeType(roleId);
    }

    public Set<DepartmentId> getDataScopeDepartmentIds(RoleId roleId) {
        return roleDataScopeRepository.findDataScopeDepartmentIds(roleId);
    }
}
