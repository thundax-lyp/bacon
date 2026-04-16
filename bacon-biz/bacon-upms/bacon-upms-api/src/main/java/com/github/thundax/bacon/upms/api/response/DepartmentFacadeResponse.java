package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentFacadeResponse {

    private DepartmentDTO department;

    public static DepartmentFacadeResponse from(DepartmentDTO department) {
        return new DepartmentFacadeResponse(department);
    }
}
