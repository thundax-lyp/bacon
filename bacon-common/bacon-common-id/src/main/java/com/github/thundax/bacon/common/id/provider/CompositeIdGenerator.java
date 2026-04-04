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

    private final List<NamedIdGenerator> generators;
    private final ApplicationEventPublisher eventPublisher;

    public CompositeIdGenerator(List<NamedIdGenerator> generators,
                                ApplicationEventPublisher eventPublisher) {
        this.generators = generators == null ? Collections.emptyList() : List.copyOf(generators);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public long nextId(String bizTag) {
        if (generators.isEmpty()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED,
                    "no id generators configured");
        }
        IdGeneratorException lastException = null;
        for (int i = 0; i < generators.size(); i++) {
            NamedIdGenerator candidate = generators.get(i);
            try {
                return candidate.generator().nextId(bizTag);
            } catch (IdGeneratorException ex) {
                lastException = ex;
                if (i < generators.size() - 1) {
                    NamedIdGenerator nextCandidate = generators.get(i + 1);
                    eventPublisher.publishEvent(new IdFallbackEvent(
                            bizTag,
                            "nextId",
                            "provider " + candidate.name() + " failed: " + ex.getMessage()
                                    + ", fallback to " + nextCandidate.name(),
                            Instant.now()));
                }
            }
        }
        throw lastException;
    }

    public record NamedIdGenerator(String name, IdGenerator generator) {
    }
}
