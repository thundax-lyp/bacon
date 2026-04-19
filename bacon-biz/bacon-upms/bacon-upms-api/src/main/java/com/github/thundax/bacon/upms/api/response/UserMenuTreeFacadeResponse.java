package com.github.thundax.bacon.upms.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuTreeFacadeResponse {

    private List<UserMenuTreeItemFacadeResponse> menus;

    public static UserMenuTreeFacadeResponse from(List<UserMenuTreeItemFacadeResponse> menus) {
        return new UserMenuTreeFacadeResponse(menus);
    }
}
