package com.github.thundax.bacon.common.core.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder.ContextSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BaconContextHolderTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldReturnSystemDefaultWhenContextMissing() {
        assertThat(BaconContextHolder.get()).isEqualTo(ContextSnapshot.systemDefault());
    }

    @Test
    void shouldCaptureAndRestoreContext() {
        ContextSnapshot snapshot = new ContextSnapshot(1001L, 2002L);
        BaconContextHolder.set(snapshot);

        ContextSnapshot captured = BaconContextHolder.capture();
        BaconContextHolder.clear();
        BaconContextHolder.restore(captured);

        assertThat(BaconContextHolder.get()).isEqualTo(snapshot);
    }

    @Test
    void shouldRestoreSystemDefaultWhenSnapshotMissing() {
        BaconContextHolder.restore(null);

        assertThat(BaconContextHolder.get()).isEqualTo(ContextSnapshot.systemDefault());
    }

    @Test
    void shouldClearContext() {
        BaconContextHolder.set(new ContextSnapshot(1001L, 2002L));

        BaconContextHolder.clear();

        assertThat(BaconContextHolder.get()).isEqualTo(ContextSnapshot.systemDefault());
    }
}
