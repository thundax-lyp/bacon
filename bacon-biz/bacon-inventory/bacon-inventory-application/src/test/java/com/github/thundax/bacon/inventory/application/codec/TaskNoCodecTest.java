package com.github.thundax.bacon.inventory.application.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;
import org.junit.jupiter.api.Test;

class TaskNoCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(TaskNoCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenValueIsBlank() {
        assertThat(TaskNoCodec.toDomain("   ")).isNull();
    }

    @Test
    void shouldConvertPlainValueToTaskNo() {
        assertThat(TaskNoCodec.toDomain("TASK-001")).isEqualTo(TaskNo.of("TASK-001"));
    }
}
