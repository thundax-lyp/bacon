package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    void shouldChangePostFields() {
        Post post = Post.create(
                PostId.of(101L), PostCode.of("OPS_LEADER"), "Ops Leader", DepartmentId.of(201L), PostStatus.ENABLED);

        post.recodeAs(PostCode.of("OPS_MANAGER"));
        post.rename("Ops Manager");
        post.assignDepartment(DepartmentId.of(202L));

        assertThat(post.getCode()).isEqualTo(PostCode.of("OPS_MANAGER"));
        assertThat(post.getName()).isEqualTo("Ops Manager");
        assertThat(post.getDepartmentId()).isEqualTo(DepartmentId.of(202L));
    }

    @Test
    void shouldTogglePostStatus() {
        Post post = Post.create(
                PostId.of(101L), PostCode.of("OPS_LEADER"), "Ops Leader", DepartmentId.of(201L), PostStatus.ENABLED);

        post.disable();
        assertThat(post.getStatus()).isEqualTo(PostStatus.DISABLED);

        post.enable();
        assertThat(post.getStatus()).isEqualTo(PostStatus.ENABLED);
    }

    @Test
    void shouldChangePostCode() {
        Post post = Post.create(
                PostId.of(101L), PostCode.of("OPS_LEADER"), "Ops Leader", DepartmentId.of(201L), PostStatus.ENABLED);

        post.recodeAs(PostCode.of("OPS_MANAGER"));

        assertThat(post.getCode()).isEqualTo(PostCode.of("OPS_MANAGER"));
    }
}
