package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class PostId extends BaseLongId {

    private PostId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PostId of(Long value) {
        return new PostId(value);
    }
}
