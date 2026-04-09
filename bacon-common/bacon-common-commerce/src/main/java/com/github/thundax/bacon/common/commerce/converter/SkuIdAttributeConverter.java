package com.github.thundax.bacon.common.commerce.converter;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.id.converter.AbstractIdAttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SkuIdAttributeConverter extends AbstractIdAttributeConverter<SkuId, Long> {

    public SkuIdAttributeConverter() {
        super(SkuId::of, SkuId::value);
    }
}
