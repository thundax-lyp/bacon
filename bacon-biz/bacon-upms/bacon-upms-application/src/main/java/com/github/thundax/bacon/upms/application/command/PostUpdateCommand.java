package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;

public record PostUpdateCommand(PostId postId, PostCode code, String name, DepartmentId departmentId, PostStatus status) {}
