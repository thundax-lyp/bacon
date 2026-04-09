package com.github.thundax.bacon.common.id.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultIdsTest {

    @Test
    void shouldGenerateTypedIdsWithStableBizTags() {
        RecordingIdGenerator idGenerator = new RecordingIdGenerator();
        DefaultIds ids = new DefaultIds(idGenerator);

        UserId userId = ids.userId();
        StoredObjectId storedObjectId = ids.storedObjectId();

        assertThat(userId).isEqualTo(UserId.of(1001L));
        assertThat(storedObjectId).isEqualTo(StoredObjectId.of(1002L));
        assertThat(idGenerator.bizTags).containsExactly("user-id", "stored-object-id");
    }

    private static final class RecordingIdGenerator implements IdGenerator {

        private final List<String> bizTags = new ArrayList<>();
        private long currentValue = 1000L;

        @Override
        public long nextId(String bizTag) {
            bizTags.add(bizTag);
            currentValue++;
            return currentValue;
        }
    }
}
