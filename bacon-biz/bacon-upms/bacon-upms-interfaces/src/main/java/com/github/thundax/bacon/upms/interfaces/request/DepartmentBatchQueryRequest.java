package com.github.thundax.bacon.upms.interfaces.request;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentBatchQueryRequest {

    private Set<Long> departmentIds;
}
