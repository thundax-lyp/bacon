package com.github.thundax.bacon.upms.application.command;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileApplicationServiceTest {

    private static final TenantId TENANT_ID = TenantId.of(1001L);
    private static final DepartmentId DEPARTMENT_ID = DepartmentId.of(11L);

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserIdentityRepository userIdentityRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private SessionCommandFacade sessionCommandFacade;
    @Mock
    private StoredObjectCommandFacade storedObjectCommandFacade;
    @Mock
    private StoredObjectReadFacade storedObjectReadFacade;
    @Mock
    private Ids ids;
    @Mock
    private IdGenerator idGenerator;

    private UserProfileApplicationService service;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContextHolder.BaconContext(TENANT_ID.value(), 2001L));
        service = new UserProfileApplicationService(
                departmentRepository,
                userRepository,
                userIdentityRepository,
                userRoleRepository,
                sessionCommandFacade,
                storedObjectCommandFacade,
                storedObjectReadFacade,
                ids,
                idGenerator);
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldClearAvatarReferenceWhenDeletingUser() {
        User user = User.reconstruct(
                UserId.of(101L),
                "Alice",
                AvatarStoredObjectNo.of("storage-20260327100000-000501"),
                DEPARTMENT_ID,
                UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));

        service.delete(UserId.of(101L));

        verify(userRepository).delete(UserId.of(101L));
        verify(storedObjectCommandFacade)
                .clearObjectReference(
                        new StoredObjectReferenceFacadeRequest(
                                "storage-20260327100000-000501", "UPMS_USER_AVATAR", "101"));
        verify(sessionCommandFacade)
                .invalidateUserSessions(new SessionInvalidateUserFacadeRequest(1001L, 101L, "USER_DELETED"));
    }
}
