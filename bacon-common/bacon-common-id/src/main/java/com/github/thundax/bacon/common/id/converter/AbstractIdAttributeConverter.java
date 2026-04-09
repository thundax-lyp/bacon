package com.github.thundax.bacon.common.id.converter;

import jakarta.persistence.AttributeConverter;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractIdAttributeConverter<I, T> implements AttributeConverter<I, T> {

    private final Function<T, I> factory;
    private final Function<I, T> extractor;

    protected AbstractIdAttributeConverter(Function<T, I> factory, Function<I, T> extractor) {
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
        this.extractor = Objects.requireNonNull(extractor, "extractor must not be null");
    }

    @Override
    public T convertToDatabaseColumn(I attribute) {
        return attribute == null ? null : extractor.apply(attribute);
    }

    @Override
    public I convertToEntityAttribute(T dbData) {
        return dbData == null ? null : factory.apply(dbData);
    }
}
