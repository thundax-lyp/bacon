package com.github.thundax.bacon.common.mybatis.fill;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MybatisPlusMetaObjectHandlerTest {

    private final MybatisPlusMetaObjectHandler handler =
            new MybatisPlusMetaObjectHandler(
                    Clock.fixed(Instant.parse("2026-04-10T10:00:00Z"), ZoneOffset.UTC), () -> "2001");

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldFillLongTenantIdOnInsertWhenEnabled() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        InsertLongTenantEntity entity = new InsertLongTenantEntity();

        handler.insertFill(metaObject(entity));

        assertThat(entity.getTenantId()).isEqualTo(1001L);
    }

    @Test
    void shouldFillDomainTenantIdOnInsertWhenEnabled() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        InsertDomainTenantEntity entity = new InsertDomainTenantEntity();

        handler.insertFill(metaObject(entity));

        assertThat(entity.getTenantId()).isEqualTo(TenantId.of(1001L));
    }

    @Test
    void shouldSkipInsertFillWhenInsertDisabled() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        ReadOnlyTenantEntity entity = new ReadOnlyTenantEntity();

        handler.insertFill(metaObject(entity));

        assertThat(entity.getTenantId()).isNull();
    }

    @Test
    void shouldVerifyTenantIdOnUpdateWhenEnabled() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        VerifiedUpdateEntity entity = new VerifiedUpdateEntity();
        entity.setTenantId(9999L);

        assertThatThrownBy(() -> handler.updateFill(metaObject(entity)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tenantId mismatch");
    }

    @Test
    void shouldSkipUpdateVerificationWhenDisabled() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        NoVerifyUpdateEntity entity = new NoVerifyUpdateEntity();
        entity.setTenantId(9999L);

        handler.updateFill(metaObject(entity));

        assertThat(entity.getTenantId()).isEqualTo(9999L);
    }

    private MetaObject metaObject(Object target) {
        return SystemMetaObject.forObject(target);
    }

    @TenantScoped(read = true, insert = true, verifyOnUpdate = false)
    static class InsertLongTenantEntity {
        private Long tenantId;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }
    }

    @TenantScoped(read = true, insert = true, verifyOnUpdate = false)
    static class InsertDomainTenantEntity {
        private TenantId tenantId;

        public TenantId getTenantId() {
            return tenantId;
        }

        public void setTenantId(TenantId tenantId) {
            this.tenantId = tenantId;
        }
    }

    @TenantScoped(read = true, insert = false, verifyOnUpdate = false)
    static class ReadOnlyTenantEntity {
        private Long tenantId;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }
    }

    @TenantScoped(read = true, insert = false, verifyOnUpdate = true)
    static class VerifiedUpdateEntity {
        private Long tenantId;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }
    }

    @TenantScoped(read = true, insert = false, verifyOnUpdate = false)
    static class NoVerifyUpdateEntity {
        private Long tenantId;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }
    }
}
