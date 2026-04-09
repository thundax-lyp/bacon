package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultIdsTest {

    @Test
    void shouldGenerateTypedIdsWithStableBizTags() {
        RecordingIdGenerator idGenerator = new RecordingIdGenerator();
        DefaultIds ids = new DefaultIds(idGenerator);

        UserId userId = ids.userId();
        UserIdentityId userIdentityId = ids.userIdentityId();
        StoredObjectId storedObjectId = ids.storedObjectId();

        assertThat(userId).isEqualTo(UserId.of(1001L));
        assertThat(userIdentityId).isEqualTo(UserIdentityId.of(1002L));
        assertThat(storedObjectId).isEqualTo(StoredObjectId.of(1003L));
        assertThat(idGenerator.bizTags).containsExactly(
                "user-id",
                "user-identity-id",
                "stored-object-id");
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
