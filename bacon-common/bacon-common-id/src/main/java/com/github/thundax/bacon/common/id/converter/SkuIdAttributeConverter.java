package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.domain.SkuId;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SkuIdAttributeConverter extends AbstractIdAttributeConverter<SkuId, Long> {

    public SkuIdAttributeConverter() {
        super(SkuId::of, SkuId::value);
    }
}
