package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.domain.TenantId;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TenantIdAttributeConverter extends AbstractIdAttributeConverter<TenantId, Long> {

    public TenantIdAttributeConverter() {
        super(TenantId::of, TenantId::value);
    }
}
