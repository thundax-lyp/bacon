package com.github.thundax.bacon.common.id.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.id.config.BaconIdGeneratorProperties;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.exception.IdGeneratorErrorCode;
import com.github.thundax.bacon.common.id.exception.IdGeneratorException;
import org.springframework.web.client.RestClient;

public class LeafIdGenerator implements IdGenerator {

    private final RestClient restClient;
    private final String pathTemplate;
    private final ObjectMapper objectMapper;

    public LeafIdGenerator(RestClient restClient,
                           BaconIdGeneratorProperties properties,
                           ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.pathTemplate = properties.getLeaf().getPathTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public long nextId(String bizTag) {
        try {
            String response = restClient.get()
                    .uri(pathTemplate, bizTag)
                    .retrieve()
                    .body(String.class);
            return parseId(response, bizTag);
        } catch (IdGeneratorException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE,
                    "leaf generate failed, bizTag=" + bizTag, ex);
        }
    }

    private long parseId(String response, String bizTag) {
        if (response == null || response.isBlank()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_RESPONSE_INVALID,
                    "leaf return empty body, bizTag=" + bizTag);
        }
        Long plainLong = tryParseLong(response.trim());
        if (plainLong != null && plainLong > 0L) {
            return plainLong;
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            Long extracted = extractId(root);
            if (extracted != null && extracted > 0L) {
                return extracted;
            }
        } catch (Exception ignored) {
            // ignore parse exception and throw unified BizException below
        }
        throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_RESPONSE_INVALID,
                "leaf return invalid body, bizTag=" + bizTag + ", body=" + response);
    }

    private Long extractId(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        if (root.isNumber()) {
            return root.longValue();
        }
        if (root.isTextual()) {
            return tryParseLong(root.textValue());
        }
        JsonNode[] candidates = new JsonNode[]{
                root.path("id"),
                root.path("value"),
                root.path("result"),
                root.path("data"),
                root.path("data").path("id"),
                root.path("data").path("value"),
                root.path("data").path("result")
        };
        for (JsonNode candidate : candidates) {
            Long value = extractId(candidate);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return null;
        }
    }
}
