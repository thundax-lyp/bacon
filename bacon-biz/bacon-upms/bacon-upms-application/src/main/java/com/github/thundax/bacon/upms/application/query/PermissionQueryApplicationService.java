package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.application.command.MenuApplicationService;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantNo;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PermissionQueryApplicationService {

    private final PermissionRepository permissionRepository;
    private final MenuApplicationService menuApplicationService;
    private final TenantRepository tenantRepository;

    public PermissionQueryApplicationService(PermissionRepository permissionRepository,
                                             MenuApplicationService menuApplicationService,
                                             TenantRepository tenantRepository) {
        this.permissionRepository = permissionRepository;
        this.menuApplicationService = menuApplicationService;
        this.tenantRepository = tenantRepository;
    }

    public List<UserMenuTreeDTO> getUserMenuTree(Long tenantId, UserId userId) {
        // 菜单树的最终组装仍由 MenuApplicationService 负责，这里只聚合权限仓储结果，不重复实现树构建规则。
        return menuApplicationService.toMenuTree(permissionRepository.getUserMenuTree(tenantId, userId));
    }

    public List<UserMenuTreeDTO> getUserMenuTree(String tenantNo, String userId) {
        return getUserMenuTree(resolveTenantIdByTenantNo(tenantNo), UserId.of(userId));
    }

    public Set<String> getUserPermissionCodes(Long tenantId, UserId userId) {
        return permissionRepository.getUserPermissionCodes(tenantId, userId);
    }

    public Set<String> getUserPermissionCodes(String tenantNo, String userId) {
        return getUserPermissionCodes(resolveTenantIdByTenantNo(tenantNo), UserId.of(userId));
    }

    public UserDataScopeDTO getUserDataScope(Long tenantId, UserId userId) {
        // 数据权限由多个维度组合而成：是否全量、范围类型集合、部门集合；查询层只做聚合，不引入额外推导。
        return new UserDataScopeDTO(permissionRepository.hasAllAccess(tenantId, userId),
                permissionRepository.getUserScopeTypes(tenantId, userId),
                permissionRepository.getUserDepartmentIds(tenantId, userId));
    }

    public UserDataScopeDTO getUserDataScope(String tenantNo, String userId) {
        return getUserDataScope(resolveTenantIdByTenantNo(tenantNo), UserId.of(userId));
    }

    private Long resolveTenantIdByTenantNo(String tenantNo) {
        if (tenantNo == null || tenantNo.isBlank()) {
            throw new IllegalArgumentException("tenantNo must not be blank");
        }
        String normalizedTenantNo = tenantNo.trim();
        return tenantRepository.findTenantByTenantNo(new TenantNo(normalizedTenantNo))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantNo));
    }
}
