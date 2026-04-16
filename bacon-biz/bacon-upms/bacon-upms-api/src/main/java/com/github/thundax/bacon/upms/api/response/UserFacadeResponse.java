package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFacadeResponse {

    private UserDTO user;

    public static UserFacadeResponse from(UserDTO user) {
        return new UserFacadeResponse(user);
    }
}
