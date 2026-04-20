package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(UserId userId);

    Optional<User> findByAccount(String account);

    List<User> list(String account, String name, String phone, UserStatus status);

    List<User> page(String account, String name, String phone, UserStatus status, int pageNo, int pageSize);

    long count(String account, String name, String phone, UserStatus status);

    boolean existsActiveByDepartmentId(DepartmentId departmentId);

    User insert(User user);

    User update(User user);

    User updatePassword(
            UserId userId, String password, boolean needChangePassword, UserCredentialId passwordCredentialIdIfAbsent);

    void delete(UserId userId);
}
