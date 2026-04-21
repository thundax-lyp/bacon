package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.upms.application.assembler.MenuAssembler;
import com.github.thundax.bacon.upms.application.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.application.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuQueryApplicationService {

    private final PermissionRepository permissionRepository;

    public MenuQueryApplicationService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<UserMenuTreeDTO> toMenuTree(List<Menu> menus) {
        // 这里假设上游已经给出树形菜单，当前方法只做 DTO 投影，不再重复构树。
        return menus.stream().map(MenuAssembler::toUserMenuTreeDto).toList();
    }

    public List<MenuTreeDTO> tree() {
        // 菜单树读取直接复用权限仓储结果，避免命令侧和权限侧各维护一套树装配逻辑。
        return permissionRepository.list().stream().map(MenuAssembler::toTreeDto).toList();
    }
}
