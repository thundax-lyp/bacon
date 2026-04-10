package com.github.thundax.bacon.boot;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.web.context.DefaultBaconContextResolver;
import com.github.thundax.bacon.common.web.context.BaconContextResolver;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration(proxyBeanMethods = false)
class BaconMonoTenantConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
    BaconContextResolver baconContextResolver(MonoTenantLookupService monoTenantLookupService) {
        return new HeaderTenantCodeBaconContextResolver(monoTenantLookupService);
    }

    @Bean
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
    MonoTenantLookupService monoTenantLookupService(TenantRepository tenantRepository) {
        return new MonoTenantLookupService(tenantRepository);
    }

    static final class HeaderTenantCodeBaconContextResolver extends DefaultBaconContextResolver {

        private static final String TENANT_CODE_HEADER = "X-Tenant-Code";

        private final MonoTenantLookupService monoTenantLookupService;

        HeaderTenantCodeBaconContextResolver(MonoTenantLookupService monoTenantLookupService) {
            this.monoTenantLookupService = monoTenantLookupService;
        }

        @Override
        public BaconContext resolve(HttpServletRequest request) {
            return new BaconContext(resolveTenantId(request), resolveUserId(request));
        }

        @Override
        protected Long resolveTenantId(HttpServletRequest request) {
            String tenantCode = currentTenantCode();
            if (tenantCode == null) {
                return super.resolveTenantId(request);
            }
            return monoTenantLookupService.findTenantIdByCode(tenantCode);
        }

        private String currentTenantCode() {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return null;
            }
            String tenantCode = attributes.getRequest().getHeader(TENANT_CODE_HEADER);
            if (tenantCode == null || tenantCode.isBlank()) {
                return null;
            }
            return tenantCode.trim();
        }
    }

    static final class MonoTenantLookupService {

        private static final int DEFAULT_EXPIRE_SECONDS = 300;

        @CreateCache(
                name = "upms:tenant:codeToId:",
                cacheType = CacheType.BOTH,
                expire = DEFAULT_EXPIRE_SECONDS,
                timeUnit = TimeUnit.SECONDS)
        private Cache<String, Long> tenantCodeToIdCache;

        private final TenantRepository tenantRepository;

        MonoTenantLookupService(TenantRepository tenantRepository) {
            this.tenantRepository = tenantRepository;
        }

        Long findTenantIdByCode(String tenantCode) {
            String normalizedTenantCode = TenantCode.of(tenantCode).value();
            Long cachedTenantId = tenantCodeToIdCache.get(normalizedTenantCode);
            if (cachedTenantId != null) {
                return cachedTenantId;
            }
            Long tenantId = tenantRepository
                    .findTenantByCode(normalizedTenantCode)
                    .map(tenant -> tenant.getId().value())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Tenant not found by tenantCode: " + normalizedTenantCode));
            tenantCodeToIdCache.put(normalizedTenantCode, tenantId);
            return tenantId;
        }
    }
}
