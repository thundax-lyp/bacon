package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuTreeFacadeResponse {

    private List<UserMenuTreeDTO> menus;

    public static UserMenuTreeFacadeResponse from(List<UserMenuTreeDTO> menus) {
        return new UserMenuTreeFacadeResponse(menus);
    }
}
