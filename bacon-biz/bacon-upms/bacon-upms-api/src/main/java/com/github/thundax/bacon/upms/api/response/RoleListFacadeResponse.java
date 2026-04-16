package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleListFacadeResponse {

    private List<RoleDTO> roles;

    public static RoleListFacadeResponse from(List<RoleDTO> roles) {
        return new RoleListFacadeResponse(roles);
    }
}
