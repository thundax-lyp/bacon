package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentBatchQueryRequest {

    private Set<@Positive(message = "departmentIds item must be greater than 0") Long> departmentIds;
}
