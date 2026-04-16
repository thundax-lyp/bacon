package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityFacadeResponse {

    private UserIdentityDTO userIdentity;

    public static UserIdentityFacadeResponse from(UserIdentityDTO userIdentity) {
        return new UserIdentityFacadeResponse(userIdentity);
    }
}
