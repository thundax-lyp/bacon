package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.entity.Menu;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuApplicationService {

    public List<UserMenuTreeDTO> toMenuTree(List<Menu> menus) {
        return menus.stream().map(this::toDto).toList();
    }

    private UserMenuTreeDTO toDto(Menu menu) {
        return new UserMenuTreeDTO(menu.getId(), menu.getName(), menu.getMenuType(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(),
                menu.getChildren() == null ? List.of() : menu.getChildren().stream().map(this::toDto).toList());
    }
}
