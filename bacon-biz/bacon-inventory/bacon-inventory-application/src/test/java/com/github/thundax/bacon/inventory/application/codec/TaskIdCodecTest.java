package com.github.thundax.bacon.inventory.application.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import org.junit.jupiter.api.Test;

class TaskIdCodecTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(TaskIdCodec.toDomain(null)).isNull();
    }

    @Test
    void shouldConvertPlainValueToTaskId() {
        assertThat(TaskIdCodec.toDomain(1002L)).isEqualTo(TaskId.of(1002L));
    }
}
