package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 岗位领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post {

    /** 岗位主键。 */
    private PostId id;
    /** 岗位编码。 */
    private PostCode code;
    /** 岗位名称。 */
    private String name;
    /** 所属部门主键。 */
    private DepartmentId departmentId;
    /** 岗位状态。 */
    private PostStatus status;

    public static Post create(PostId id, PostCode code, String name, DepartmentId departmentId, PostStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Post(id, code, name, departmentId, status);
    }

    public static Post reconstruct(PostId id, PostCode code, String name, DepartmentId departmentId, PostStatus status) {
        return new Post(id, code, name, departmentId, status);
    }

    public void update(PostCode code, String name, DepartmentId departmentId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
    }

    public void enable() {
        this.status = PostStatus.ENABLED;
    }

    public void disable() {
        this.status = PostStatus.DISABLED;
    }

    public void changeCode(PostCode code) {
        Objects.requireNonNull(code, "code must not be null");
        this.code = code;
    }
}
