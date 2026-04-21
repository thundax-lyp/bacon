package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.application.assembler.UserDataScopeAssembler;
import com.github.thundax.bacon.upms.application.query.MenuQueryApplicationService;
import com.github.thundax.bacon.upms.application.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.application.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PermissionQueryApplicationService {

    private final PermissionRepository permissionRepository;
    private final MenuQueryApplicationService menuQueryApplicationService;

    public PermissionQueryApplicationService(
            PermissionRepository permissionRepository, MenuQueryApplicationService menuQueryApplicationService) {
        this.permissionRepository = permissionRepository;
        this.menuQueryApplicationService = menuQueryApplicationService;
    }

    public List<UserMenuTreeDTO> listMenuTreeByUserId(UserId userId) {
        // 菜单树的最终组装仍由 MenuQueryApplicationService 负责，这里只聚合权限仓储结果，不重复实现树构建规则。
        return menuQueryApplicationService.toMenuTree(permissionRepository.listMenuTreeByUserId(userId));
    }

    public Set<String> findPermissionCodesByUserId(UserId userId) {
        return permissionRepository.findPermissionCodesByUserId(userId);
    }

    public UserDataScopeDTO getUserDataScope(UserId userId) {
        // 数据权限由多个维度组合而成：是否全量、范围类型集合、部门集合；查询层只做聚合，不引入额外推导。
        return UserDataScopeAssembler.toDto(
                permissionRepository.existsAllAccessByUserId(userId),
                permissionRepository.findScopeTypesByUserId(userId),
                permissionRepository.findDepartmentIdsByUserId(userId));
    }
}
