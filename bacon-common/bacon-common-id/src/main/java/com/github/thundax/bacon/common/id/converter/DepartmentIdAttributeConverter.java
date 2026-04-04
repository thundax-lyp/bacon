package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DepartmentIdAttributeConverter extends AbstractIdAttributeConverter<DepartmentId, Long> {

    public DepartmentIdAttributeConverter() {
        super(DepartmentId::of, DepartmentId::value);
    }
}
