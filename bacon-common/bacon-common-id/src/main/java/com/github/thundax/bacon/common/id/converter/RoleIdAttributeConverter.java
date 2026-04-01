package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.domain.RoleId;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleIdAttributeConverter extends AbstractIdAttributeConverter<RoleId, String> {

    public RoleIdAttributeConverter() {
        super(RoleId::of, RoleId::value);
    }
}
