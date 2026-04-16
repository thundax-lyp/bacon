package com.github.thundax.bacon.upms.api.request;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentListFacadeRequest {

    private Set<Long> departmentIds;
}
