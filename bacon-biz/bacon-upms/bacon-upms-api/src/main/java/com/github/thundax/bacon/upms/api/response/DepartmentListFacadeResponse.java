package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentListFacadeResponse {

    private List<DepartmentDTO> departments;

    public static DepartmentListFacadeResponse from(List<DepartmentDTO> departments) {
        return new DepartmentListFacadeResponse(departments);
    }
}
