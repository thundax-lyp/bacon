package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleFacadeResponse {

    private RoleDTO role;

    public static RoleFacadeResponse from(RoleDTO role) {
        return new RoleFacadeResponse(role);
    }
}
