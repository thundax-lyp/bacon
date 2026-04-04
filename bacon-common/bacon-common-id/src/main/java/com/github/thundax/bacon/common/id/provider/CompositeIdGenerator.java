package com.github.thundax.bacon.common.id.provider;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.event.IdFallbackEvent;
import com.github.thundax.bacon.common.id.exception.IdGeneratorErrorCode;
import com.github.thundax.bacon.common.id.exception.IdGeneratorException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;

public class CompositeIdGenerator implements IdGenerator {

    private final List<NamedIdGenerator> primaryGenerators;
    private final IdGenerator snowflakeFallback;
    private final ApplicationEventPublisher eventPublisher;
    private final boolean fallbackEnabled;

    public CompositeIdGenerator(List<NamedIdGenerator> primaryGenerators,
                                IdGenerator snowflakeFallback,
                                ApplicationEventPublisher eventPublisher,
                                boolean fallbackEnabled) {
        this.primaryGenerators = primaryGenerators == null ? Collections.emptyList() : List.copyOf(primaryGenerators);
        this.snowflakeFallback = snowflakeFallback;
        this.eventPublisher = eventPublisher;
        this.fallbackEnabled = fallbackEnabled;
    }

    @Override
    public long nextId(String bizTag) {
        if (primaryGenerators.isEmpty()) {
            publishFallbackEvent(bizTag, "no primary id generators configured");
            return fallbackOrThrow(bizTag, null);
        }
        IdGeneratorException lastException = null;
        for (int i = 0; i < primaryGenerators.size(); i++) {
            NamedIdGenerator candidate = primaryGenerators.get(i);
            try {
                return candidate.generator().nextId(bizTag);
            } catch (IdGeneratorException ex) {
                lastException = ex;
                if (i < primaryGenerators.size() - 1) {
                    NamedIdGenerator nextCandidate = primaryGenerators.get(i + 1);
                    publishFallbackEvent(
                            bizTag,
                            "provider " + candidate.name() + " failed: " + ex.getMessage()
                                    + ", fallback to " + nextCandidate.name());
                } else {
                    eventPublisher.publishEvent(new IdFallbackEvent(
                            bizTag,
                            "nextId",
                            "provider " + candidate.name() + " failed: " + ex.getMessage()
                                    + ", fallback to snowflake",
                            Instant.now()));
                }
            }
        }
        return fallbackOrThrow(bizTag, lastException);
    }

    private long fallbackOrThrow(String bizTag, IdGeneratorException lastException) {
        if (!fallbackEnabled) {
            throw lastException == null
                    ? new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED,
                    "no primary id generators configured and fallback disabled")
                    : lastException;
        }
        try {
            return snowflakeFallback.nextId(bizTag);
        } catch (IdGeneratorException ex) {
            publishFallbackEvent(bizTag, "snowflake fallback failed: " + ex.getMessage());
            throw ex;
        }
    }

    private void publishFallbackEvent(String bizTag, String reason) {
        eventPublisher.publishEvent(new IdFallbackEvent(
                bizTag,
                "nextId",
                reason,
                Instant.now()));
    }

    public record NamedIdGenerator(String name, IdGenerator generator) {
    }
}
