package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import org.junit.jupiter.api.Test;

class ResourceTest {

    @Test
    void shouldChangeResourceFields() {
        Resource resource = Resource.create(
                ResourceId.of(101L),
                ResourceCode.of("user:read"),
                "Read User",
                ResourceType.API,
                "GET",
                "/sys/users/{id}",
                ResourceStatus.ENABLED);

        resource.recodeAs(ResourceCode.of("user:write"));
        resource.rename("Write User");
        resource.classifyAs(ResourceType.API);
        resource.exposeEndpoint("POST", "/sys/users");

        assertThat(resource.getCode()).isEqualTo(ResourceCode.of("user:write"));
        assertThat(resource.getName()).isEqualTo("Write User");
        assertThat(resource.getResourceType()).isEqualTo(ResourceType.API);
        assertThat(resource.getHttpMethod()).isEqualTo("POST");
        assertThat(resource.getUri()).isEqualTo("/sys/users");
    }

    @Test
    void shouldToggleResourceStatus() {
        Resource resource = Resource.create(
                ResourceId.of(101L),
                ResourceCode.of("user:read"),
                "Read User",
                ResourceType.API,
                "GET",
                "/sys/users/{id}",
                ResourceStatus.ENABLED);

        resource.disable();
        assertThat(resource.getStatus()).isEqualTo(ResourceStatus.DISABLED);

        resource.enable();
        assertThat(resource.getStatus()).isEqualTo(ResourceStatus.ENABLED);
    }

    @Test
    void shouldChangeResourceCode() {
        Resource resource = Resource.create(
                ResourceId.of(101L),
                ResourceCode.of("user:read"),
                "Read User",
                ResourceType.API,
                "GET",
                "/sys/users/{id}",
                ResourceStatus.ENABLED);

        resource.recodeAs(ResourceCode.of("user:write"));

        assertThat(resource.getCode()).isEqualTo(ResourceCode.of("user:write"));
    }
}
