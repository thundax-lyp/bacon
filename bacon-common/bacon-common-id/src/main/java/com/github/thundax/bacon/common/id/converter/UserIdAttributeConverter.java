package com.github.thundax.bacon.common.id.converter;

import com.github.thundax.bacon.common.id.domain.UserId;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UserIdAttributeConverter extends AbstractIdAttributeConverter<UserId, Long> {

    public UserIdAttributeConverter() {
        super(UserId::of, UserId::value);
    }
}
