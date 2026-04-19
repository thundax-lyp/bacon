package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentBatchQueryRequest {

    private Set<@Positive(message = "departmentIds item must be greater than 0") Long> departmentIds;
}
