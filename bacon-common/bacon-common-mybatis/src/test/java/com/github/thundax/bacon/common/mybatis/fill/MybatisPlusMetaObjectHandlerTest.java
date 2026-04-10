package com.github.thundax.bacon.common.mybatis.fill;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantIsolated;
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
    void shouldFillLongTenantIdForAnnotatedEntity() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        LongTenantEntity entity = new LongTenantEntity();

        handler.insertFill(metaObject(entity));

        assertThat(entity.getTenantId()).isEqualTo(1001L);
    }

    @Test
    void shouldFillDomainTenantIdForAnnotatedEntity() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        DomainTenantEntity entity = new DomainTenantEntity();

        handler.insertFill(metaObject(entity));

        assertThat(entity.getTenantId()).isEqualTo(TenantId.of(1001L));
    }

    @Test
    void shouldNotOverrideExistingTenantId() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        LongTenantEntity entity = new LongTenantEntity();
        entity.setTenantId(9999L);

        handler.insertFill(metaObject(entity));

        assertThat(entity.getTenantId()).isEqualTo(9999L);
    }

    @Test
    void shouldIgnoreNonAnnotatedEntity() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        PlainEntity entity = new PlainEntity();

        handler.insertFill(metaObject(entity));

        assertThat(entity.getTenantId()).isNull();
    }

    private MetaObject metaObject(Object target) {
        return SystemMetaObject.forObject(target);
    }

    @TenantIsolated
    static class LongTenantEntity {
        private Long tenantId;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }
    }

    @TenantIsolated
    static class DomainTenantEntity {
        private TenantId tenantId;

        public TenantId getTenantId() {
            return tenantId;
        }

        public void setTenantId(TenantId tenantId) {
            this.tenantId = tenantId;
        }
    }

    static class PlainEntity {
        private Long tenantId;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }
    }
}
