package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskIdMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(TaskIdMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToTaskId() {
        assertThat(TaskIdMapper.toDomain(1002L))
                .isEqualTo(TaskId.of(1002L));
    }
}
