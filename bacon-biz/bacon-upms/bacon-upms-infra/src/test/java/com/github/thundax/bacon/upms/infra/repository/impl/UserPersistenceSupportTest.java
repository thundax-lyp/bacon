package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserCredentialMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        support = new UserPersistenceSupport(userMapper, userIdentityMapper, userCredentialMapper);
    }

    @Test
    void shouldInsertUserAndMapGeneratedId() {
        ArgumentCaptor<UserDO> captor = ArgumentCaptor.forClass(UserDO.class);
        User newUser = new User(null, 1L, "alice", "Alice", 9001L, "13800000001", "ENC", 11L,
                UserStatus.ENABLED);
        UserId generatedId = UserId.of("U101");

        when(userMapper.insert(any(UserDO.class))).thenAnswer(invocation -> {
            UserDO dataObject = invocation.getArgument(0);
            dataObject.setId(generatedId);
            return 1;
        });

        User savedUser = support.saveUser(newUser);

        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
        assertThat(captor.getValue().getAvatarObjectId()).isEqualTo(9001L);
        assertThat(savedUser.getId()).isEqualTo(generatedId);
        assertThat(savedUser.getAccount()).isEqualTo("alice");
        assertThat(savedUser.getAvatarObjectId()).isEqualTo(9001L);
    }
}
