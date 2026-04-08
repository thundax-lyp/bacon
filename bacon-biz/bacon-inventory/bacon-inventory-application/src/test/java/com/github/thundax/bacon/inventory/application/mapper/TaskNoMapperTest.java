package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskNoMapperTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(TaskNoMapper.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(TaskNoMapper.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToTaskNo() {
        assertThat(TaskNoMapper.toDomain("TASK-001"))
                .isEqualTo(TaskNo.of("TASK-001"));
    }
}
