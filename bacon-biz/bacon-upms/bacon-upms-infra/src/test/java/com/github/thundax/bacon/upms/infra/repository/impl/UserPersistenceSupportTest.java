package com.github.thundax.bacon.upms.infra.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserCredentialMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPersistenceSupportTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserIdentityMapper userIdentityMapper;

    @Mock
    private UserCredentialMapper userCredentialMapper;

    private UserPersistenceSupport support;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContextHolder.BaconContext(1001L, 2001L));
        support = new UserPersistenceSupport(userMapper, userIdentityMapper, userCredentialMapper);
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldInsertUserAndMapGeneratedId() {
        ArgumentCaptor<UserDO> captor = ArgumentCaptor.forClass(UserDO.class);
        User newUser = User.create(
                UserId.of(101L), "Alice", StoredObjectId.of(9001L), DepartmentId.of(11L), UserStatus.ENABLED);
        UserId generatedId = UserId.of(101L);

        when(userMapper.insert(any(UserDO.class))).thenAnswer(invocation -> {
            UserDO dataObject = invocation.getArgument(0);
            dataObject.setId(generatedId.value());
            return 1;
        });

        User savedUser = support.saveUser(newUser);

        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getDeleted()).isFalse();
        assertThat(captor.getValue().getAvatarObjectId()).isEqualTo(9001L);
        assertThat(savedUser.getId()).isEqualTo(generatedId);
        assertThat(savedUser.getName()).isEqualTo("Alice");
        assertThat(savedUser.getAvatarObjectId()).isEqualTo(StoredObjectId.of(9001L));
    }
}
