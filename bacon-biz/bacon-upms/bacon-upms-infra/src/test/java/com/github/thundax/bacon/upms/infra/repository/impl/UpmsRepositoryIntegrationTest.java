package com.github.thundax.bacon.upms.infra.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.DefaultIds;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleDataScopeRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleMenuRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleResourceRepository;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRoleRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import com.github.thundax.bacon.upms.infra.persistence.handler.DepartmentIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.MenuIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.PostIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.RoleIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.UserCredentialIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.UserIdentityIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DataPermissionRuleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DepartmentMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.MenuMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.ResourceMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleDataScopeRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMenuRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleResourceRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserCredentialMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

class UpmsRepositoryIntegrationTest {

    private static final TenantId TENANT_ID = TenantId.of(1001L);
    private static final DepartmentId HEADQUARTERS_DEPARTMENT_ID = DepartmentId.of(1001L);
    private static final DepartmentId OPERATIONS_DEPARTMENT_ID = DepartmentId.of(1002L);
    private static final DepartmentId CHILD_DEPARTMENT_ID = DepartmentId.of(1003L);

    private static final org.springframework.context.annotation.AnnotationConfigApplicationContext CONTEXT =
            new org.springframework.context.annotation.AnnotationConfigApplicationContext(TestConfig.class);

    private final DataSource dataSource = CONTEXT.getBean(DataSource.class);
    private final UserRepository userRepository = CONTEXT.getBean(UserRepository.class);
    private final UserIdentityRepository userIdentityRepository = CONTEXT.getBean(UserIdentityRepository.class);
    private final UserCredentialRepository userCredentialRepository = CONTEXT.getBean(UserCredentialRepository.class);
    private final UserRoleRepository userRoleRepository = CONTEXT.getBean(UserRoleRepository.class);
    private final RoleRepository roleRepository = CONTEXT.getBean(RoleRepository.class);
    private final RoleMenuRepository roleMenuRepository = CONTEXT.getBean(RoleMenuRepository.class);
    private final RoleResourceRepository roleResourceRepository = CONTEXT.getBean(RoleResourceRepository.class);
    private final RoleDataScopeRepository roleDataScopeRepository = CONTEXT.getBean(RoleDataScopeRepository.class);
    private final MenuRepository menuRepository = CONTEXT.getBean(MenuRepository.class);
    private final ResourceRepository resourceRepository = CONTEXT.getBean(ResourceRepository.class);
    private final PermissionRepository permissionRepository = CONTEXT.getBean(PermissionRepository.class);
    private final DepartmentRepository departmentRepository = CONTEXT.getBean(DepartmentRepository.class);

    @BeforeEach
    void setUpSchema() throws Exception {
        BaconContextHolder.set(new BaconContextHolder.BaconContext(TENANT_ID.value(), 2001L));
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role_data_scope_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_data_permission_rule");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role_resource_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role_menu_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_user_role_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_user_credential");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_user_identity");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_resource");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_menu");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_user");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_department");

            statement.execute(
                    """
                    CREATE TABLE bacon_upms_department (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        code varchar(64) NOT NULL,
                        name varchar(128) NOT NULL,
                        parent_id varchar(64) NULL,
                        leader_user_id varchar(64) NULL,
                        sort int NOT NULL,
                        status varchar(16) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_user (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        name varchar(128) NOT NULL,
                        avatar_object_id varchar(64) NULL,
                        department_id varchar(64) NULL,
                        status varchar(16) NOT NULL,
                        deleted boolean NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_user_identity (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        user_id varchar(64) NOT NULL,
                        identity_type varchar(32) NOT NULL,
                        identity_value varchar(128) NOT NULL,
                        status varchar(16) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_user_credential (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        user_id varchar(64) NOT NULL,
                        identity_id varchar(64) NULL,
                        credential_type varchar(32) NOT NULL,
                        factor_level varchar(16) NOT NULL,
                        credential_value varchar(255) NOT NULL,
                        status varchar(16) NOT NULL,
                        need_change_password boolean NOT NULL,
                        failed_count int NOT NULL,
                        failed_limit int NOT NULL,
                        lock_reason varchar(64) NULL,
                        locked_until timestamp NULL,
                        expires_at timestamp NULL,
                        last_verified_at timestamp NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_role (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        code varchar(64) NOT NULL,
                        name varchar(128) NOT NULL,
                        role_type varchar(32) NOT NULL,
                        data_scope_type varchar(32) NOT NULL,
                        status varchar(16) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_menu (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        menu_type varchar(32) NOT NULL,
                        name varchar(128) NOT NULL,
                        parent_id varchar(64) NULL,
                        route_path varchar(255) NULL,
                        component_name varchar(255) NULL,
                        icon varchar(128) NULL,
                        sort int NOT NULL,
                        permission_code varchar(128) NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_resource (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        code varchar(128) NOT NULL,
                        name varchar(128) NOT NULL,
                        resource_type varchar(32) NOT NULL,
                        method varchar(16) NULL,
                        path varchar(255) NULL,
                        status varchar(16) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_user_role_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id varchar(64) NOT NULL,
                        user_id varchar(64) NOT NULL,
                        role_id varchar(64) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_role_menu_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id varchar(64) NOT NULL,
                        role_id varchar(64) NOT NULL,
                        menu_id varchar(64) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_role_resource_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id varchar(64) NOT NULL,
                        role_id varchar(64) NOT NULL,
                        resource_id varchar(64) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_data_permission_rule (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id varchar(64) NOT NULL,
                        role_id varchar(64) NOT NULL,
                        data_scope_type varchar(32) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE bacon_upms_role_data_scope_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id varchar(64) NOT NULL,
                        role_id varchar(64) NOT NULL,
                        department_id varchar(64) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
        }
    }

    @AfterEach
    void clearContext() {
        BaconContextHolder.clear();
    }

    @AfterAll
    static void closeContext() {
        CONTEXT.close();
    }

    private User createUser(
            User user,
            String account,
            String phone,
            UserIdentityId accountIdentityId,
            UserIdentityId phoneIdentityId,
            UserCredentialId passwordCredentialId) {
        User savedUser = userRepository.insert(user);
        UserIdentity accountIdentity = userIdentityRepository.insert(
                UserIdentity.create(accountIdentityId, savedUser.getId(), UserIdentityType.ACCOUNT, account));
        if (phone != null) {
            userIdentityRepository.insert(
                    UserIdentity.create(phoneIdentityId, savedUser.getId(), UserIdentityType.PHONE, phone));
        }
        userCredentialRepository.insert(UserCredential.createPassword(
                passwordCredentialId,
                savedUser.getId(),
                accountIdentity.getId(),
                CONTEXT.getBean(PasswordEncoder.class).encode("123456"),
                true,
                5,
                Instant.now().plus(90L, ChronoUnit.DAYS)));
        return savedUser;
    }

    private User updateUser(
            User user, String account, String phone, UserIdentityId accountIdentityId, UserIdentityId phoneIdentityId) {
        User savedUser = userRepository.update(user);
        UserIdentity currentAccountIdentity = userIdentityRepository
                .findIdentityByUserId(savedUser.getId(), UserIdentityType.ACCOUNT)
                .orElse(null);
        if (currentAccountIdentity == null) {
            userIdentityRepository.insert(
                    UserIdentity.create(accountIdentityId, savedUser.getId(), UserIdentityType.ACCOUNT, account));
        } else {
            currentAccountIdentity.changeAccount(account);
            userIdentityRepository.update(currentAccountIdentity);
        }
        if (phone == null) {
            userIdentityRepository.deleteIdentityByUserIdAndType(savedUser.getId(), UserIdentityType.PHONE);
            return savedUser;
        }
        UserIdentity currentPhoneIdentity = userIdentityRepository
                .findIdentityByUserId(savedUser.getId(), UserIdentityType.PHONE)
                .orElse(null);
        if (currentPhoneIdentity == null) {
            userIdentityRepository.insert(
                    UserIdentity.create(phoneIdentityId, savedUser.getId(), UserIdentityType.PHONE, phone));
            return savedUser;
        }
        currentPhoneIdentity.changePhone(phone);
        userIdentityRepository.update(currentPhoneIdentity);
        return savedUser;
    }

    private boolean isUserDeleted(String userId) {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet =
                        statement.executeQuery("SELECT deleted FROM bacon_upms_user WHERE id = '" + userId + "'")) {
            if (!resultSet.next()) {
                return false;
            }
            return resultSet.getBoolean(1);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to query user deleted flag", ex);
        }
    }

    @Test
    void shouldPersistUserRoleAndPermissionGraph() {
        Department rootDepartment = Department.create(
                HEADQUARTERS_DEPARTMENT_ID, DepartmentCode.of("ROOT"), "Headquarters", null, null);
        rootDepartment.sort(1);
        rootDepartment = departmentRepository.insert(rootDepartment);
        Department childDepartment =
                Department.create(
                        OPERATIONS_DEPARTMENT_ID,
                        DepartmentCode.of("OPS"),
                        "Operations",
                        rootDepartment.getId(),
                        null);
        childDepartment.sort(2);
        childDepartment = departmentRepository.insert(childDepartment);
        Menu rootMenu = menuRepository.insert(Menu.create(
                MenuId.of(2001L), MenuType.MENU, "System", null, "/system", "SystemPage", "shield", null));
        Menu childMenu = menuRepository.insert(Menu.create(
                MenuId.of(2002L),
                MenuType.MENU,
                "Users",
                rootMenu.getId(),
                "/system/users",
                "UserPage",
                "user",
                "upms:user:view"));
        Resource resource = resourceRepository.insert(Resource.create(
                ResourceId.of(2301L),
                ResourceCode.of("upms:user:edit"),
                "Edit User",
                ResourceType.API,
                "POST",
                "/users"));
        Role role = roleRepository.insert(Role.create(
                RoleId.of(2101L), RoleCode.of("ADMIN"), "Administrator", RoleType.SYSTEM_ROLE, RoleDataScopeType.SELF));
        User user = createUser(
                User.create(
                        UserId.of(2201L),
                        "Alice",
                        AvatarStoredObjectNo.of("storage-20260327100000-000901"),
                        childDepartment.getId()),
                "alice",
                "13800000001",
                UserIdentityId.of(3001L),
                UserIdentityId.of(3002L),
                UserCredentialId.of(3003L));

        roleMenuRepository.updateMenuIds(role.getId(), Set.of(rootMenu.getId(), childMenu.getId()));
        roleResourceRepository.updateResourceCodes(role.getId(), Set.of(resource.getCode()));
        roleDataScopeRepository.updateDataScope(
                role.getId(),
                RoleDataScopeType.CUSTOM,
                Set.of(rootDepartment.getId(), childDepartment.getId()));
        userRoleRepository.updateRoleIds(user.getId(), List.of(role.getId()));

        assertEquals(1, permissionRepository.list().size());

        User persistedUser = userRepository.findByAccount("alice").orElseThrow();
        assertNotNull(persistedUser.getId());
        assertTrue(persistedUser.getId().value() > 0);
        assertEquals(
                AvatarStoredObjectNo.of("storage-20260327100000-000901"), persistedUser.getAvatarStoredObjectNo());
        assertTrue(userIdentityRepository
                .findIdentity(UserIdentityType.ACCOUNT, "alice")
                .isPresent());
        assertNotNull(userCredentialRepository
                .findCredentialByUserId(persistedUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue());
        assertTrue(userCredentialRepository
                .findCredentialByUserId(persistedUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .isNeedChangePassword());
        assertTrue(userIdentityRepository
                .findIdentity(UserIdentityType.PHONE, "13800000001")
                .isPresent());
        assertEquals(1L, userRepository.count("ali", null, null, UserStatus.ACTIVE));

        List<Menu> menuTree = permissionRepository.listMenuTreeByUserId(user.getId());
        assertEquals(1, menuTree.size());
        assertEquals(rootMenu.getId(), menuTree.get(0).getId());
        assertEquals(1, menuTree.get(0).getChildren().size());
        assertEquals(childMenu.getId(), menuTree.get(0).getChildren().get(0).getId());

        Set<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());
        assertTrue(permissionCodes.contains("upms:user:view"));
        assertTrue(permissionCodes.contains("upms:user:edit"));
        assertEquals(Set.of("CUSTOM"), permissionRepository.findScopeTypesByUserId(user.getId()));
        assertEquals(
                Set.of(rootDepartment.getId(), childDepartment.getId()),
                permissionRepository.findDepartmentIdsByUserId(user.getId()));
        assertFalse(permissionRepository.existsAllAccessByUserId(user.getId()));
    }

    @Test
    void shouldReplacePhoneIdentityAndClearUserAssignmentsOnDelete() {
        Department department = Department.create(
                OPERATIONS_DEPARTMENT_ID, DepartmentCode.of("OPS"), "Operations", null, null);
        department.sort(1);
        department = departmentRepository.insert(department);
        Role role = roleRepository.insert(Role.create(
                RoleId.of(2102L), RoleCode.of("OPS_ADMIN"), "Ops Admin", RoleType.SYSTEM_ROLE, RoleDataScopeType.SELF));
        User createdUser = createUser(
                User.create(
                        UserId.of(2202L),
                        "Bob",
                        AvatarStoredObjectNo.of("storage-20260327100000-001001"),
                        department.getId()),
                "bob",
                "13800000002",
                UserIdentityId.of(3101L),
                UserIdentityId.of(3102L),
                UserCredentialId.of(3103L));

        userRoleRepository.updateRoleIds(createdUser.getId(), List.of(role.getId()));
        UserIdentity originalAccountIdentity = userIdentityRepository
                .findIdentityByUserId(createdUser.getId(), UserIdentityType.ACCOUNT)
                .orElseThrow();
        UserIdentity originalPhoneIdentity = userIdentityRepository
                .findIdentityByUserId(createdUser.getId(), UserIdentityType.PHONE)
                .orElseThrow();
        User updatedUser = updateUser(
                User.reconstruct(
                        createdUser.getId(),
                        "Bob",
                        AvatarStoredObjectNo.of("storage-20260327100000-001002"),
                        department.getId(),
                        UserStatus.ACTIVE),
                "bob",
                "13900000003",
                UserIdentityId.of(3201L),
                UserIdentityId.of(3202L));

        assertFalse(userIdentityRepository
                .findIdentity(UserIdentityType.PHONE, "13800000002")
                .isPresent());
        assertTrue(userIdentityRepository
                .findIdentity(UserIdentityType.PHONE, "13900000003")
                .isPresent());
        assertEquals(
                originalAccountIdentity.getId(),
                userIdentityRepository
                        .findIdentityByUserId(updatedUser.getId(), UserIdentityType.ACCOUNT)
                        .orElseThrow()
                        .getId());
        assertEquals(
                originalPhoneIdentity.getId(),
                userIdentityRepository
                        .findIdentityByUserId(updatedUser.getId(), UserIdentityType.PHONE)
                        .orElseThrow()
                        .getId());
        assertNotNull(userCredentialRepository
                .findCredentialByUserId(updatedUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue());
        assertEquals(
                AvatarStoredObjectNo.of("storage-20260327100000-001002"),
                userRepository.findById(updatedUser.getId()).orElseThrow().getAvatarStoredObjectNo());
        assertTrue(departmentRepository.existsUser(department.getId()));

        userRepository.delete(updatedUser.getId());

        assertFalse(userRepository.findById(updatedUser.getId()).isPresent());
        assertFalse(
                userIdentityRepository.findIdentity(UserIdentityType.ACCOUNT, "bob").isPresent());
        assertTrue(roleRepository.findByUserId(updatedUser.getId()).isEmpty());
        assertFalse(departmentRepository.existsUser(department.getId()));
        assertTrue(isUserDeleted(String.valueOf(updatedUser.getId().value())));
    }

    @Test
    void shouldSyncAccountIdentityPasswordWhenUpdatingPassword() {
        Department department = Department.create(
                OPERATIONS_DEPARTMENT_ID, DepartmentCode.of("OPS"), "Operations", null, null);
        department.sort(1);
        department = departmentRepository.insert(department);
        User createdUser = createUser(
                User.create(UserId.of(2203L), "Carol", null, department.getId()),
                "carol",
                "13600000001",
                UserIdentityId.of(3301L),
                UserIdentityId.of(3302L),
                UserCredentialId.of(3303L));

        String originalPasswordHash = userCredentialRepository
                .findCredentialByUserId(createdUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue();

        User updatedUser =
                userRepository.updatePassword(
                        createdUser.getId(),
                        CONTEXT.getBean(PasswordEncoder.class).encode("654321"),
                        false,
                        5,
                        Instant.now().plus(90, ChronoUnit.DAYS),
                        UserCredentialId.of(3304L));

        String updatedPasswordHash = userCredentialRepository
                .findCredentialByUserId(createdUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue();
        assertNotEquals(originalPasswordHash, updatedPasswordHash);
        assertFalse(userCredentialRepository
                .findCredentialByUserId(createdUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .isNeedChangePassword());
    }

    @Test
    void shouldReplaceRoleRelationsAndSupportDepartmentHierarchyQueries() {
        Department root = Department.create(
                HEADQUARTERS_DEPARTMENT_ID, DepartmentCode.of("ROOT"), "Root", null, null);
        root.sort(1);
        root = departmentRepository.insert(root);
        Department child = Department.create(
                CHILD_DEPARTMENT_ID, DepartmentCode.of("CHILD"), "Child", root.getId(), null);
        child.sort(2);
        child = departmentRepository.insert(child);
        Menu oldMenu = menuRepository.insert(Menu.create(
                MenuId.of(2003L), MenuType.MENU, "Old", null, "/old", "OldPage", "archive", "upms:old:view"));
        Menu newMenu = menuRepository.insert(Menu.create(
                MenuId.of(2004L), MenuType.MENU, "New", null, "/new", "NewPage", "star", "upms:new:view"));
        Resource oldResource = resourceRepository.insert(Resource.create(
                ResourceId.of(2302L),
                ResourceCode.of("upms:old:edit"),
                "Old Edit",
                ResourceType.API,
                "POST",
                "/old"));
        Resource newResource = resourceRepository.insert(Resource.create(
                ResourceId.of(2303L),
                ResourceCode.of("upms:new:edit"),
                "New Edit",
                ResourceType.API,
                "PUT",
                "/new"));
        Role role = roleRepository.insert(Role.create(
                RoleId.of(2103L), RoleCode.of("MANAGER"), "Manager", RoleType.SYSTEM_ROLE, RoleDataScopeType.SELF));

        roleMenuRepository.updateMenuIds(role.getId(), Set.of(oldMenu.getId()));
        roleResourceRepository.updateResourceCodes(role.getId(), Set.of(oldResource.getCode()));
        roleDataScopeRepository.updateDataScope(
                role.getId(), RoleDataScopeType.CUSTOM, Set.of(root.getId()));

        User user = createUser(
                User.create(UserId.of(2204L), "Manager", null, child.getId()),
                "manager",
                "13700000001",
                UserIdentityId.of(3401L),
                UserIdentityId.of(3402L),
                UserCredentialId.of(3403L));
        userRoleRepository.updateRoleIds(user.getId(), List.of(role.getId()));

        assertEquals(
                Set.of("upms:old:view", "upms:old:edit"), permissionRepository.findPermissionCodesByUserId(user.getId()));
        assertEquals(Set.of("CUSTOM"), permissionRepository.findScopeTypesByUserId(user.getId()));
        assertEquals(1, permissionRepository.listMenuTreeByUserId(user.getId()).size());

        roleMenuRepository.updateMenuIds(role.getId(), Set.of(newMenu.getId()));
        roleResourceRepository.updateResourceCodes(role.getId(), Set.of(newResource.getCode()));
        roleDataScopeRepository.updateDataScope(
                role.getId(), RoleDataScopeType.ALL, Set.of(child.getId()));

        assertEquals(Set.of(newMenu.getId()), roleMenuRepository.findMenuIds(role.getId()));
        assertEquals(Set.of(newResource.getCode()), roleResourceRepository.findResourceCodes(role.getId()));
        assertEquals(RoleDataScopeType.ALL, roleDataScopeRepository.findDataScopeType(role.getId()));
        assertTrue(roleDataScopeRepository.findDataScopeDepartmentIds(role.getId()).isEmpty());
        assertEquals(
                Set.of("upms:new:view", "upms:new:edit"), permissionRepository.findPermissionCodesByUserId(user.getId()));
        assertEquals(Set.of("ALL"), permissionRepository.findScopeTypesByUserId(user.getId()));
        assertTrue(departmentRepository.existsChild(root.getId()));
        assertEquals(
                Set.of(root.getId(), child.getId()),
                departmentRepository.listByIds(Set.of(root.getId(), child.getId())).stream()
                        .map(Department::getId)
                        .collect(java.util.stream.Collectors.toSet()));

        menuRepository.delete(newMenu.getId());
        resourceRepository.delete(newResource.getId());

        assertTrue(roleMenuRepository.findMenuIds(role.getId()).isEmpty());
        assertTrue(roleResourceRepository.findResourceCodes(role.getId()).isEmpty());
        assertTrue(permissionRepository.findPermissionCodesByUserId(user.getId()).isEmpty());
        assertNotEquals(oldMenu.getId(), newMenu.getId());
        assertNotEquals(oldResource.getId(), newResource.getId());
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan("com.github.thundax.bacon.upms.infra.persistence.mapper")
    static class TestConfig {

        private static final int TEST_CACHE_LIMIT = 1_000;

        @Bean
        DataSource dataSource() {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:upms_repo_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
            factoryBean.setDataSource(dataSource);
            factoryBean.setTypeHandlersPackage("com.github.thundax.bacon.common.mybatis.handler");
            MybatisConfiguration configuration = new MybatisConfiguration();
            configuration.getTypeHandlerRegistry().register(DepartmentId.class, DepartmentIdTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(MenuId.class, MenuIdTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(PostId.class, PostIdTypeHandler.class);
            configuration
                    .getTypeHandlerRegistry()
                    .register(
                            com.github.thundax.bacon.upms.domain.model.valueobject.RoleId.class,
                            RoleIdTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(UserIdentityId.class, UserIdentityIdTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(UserCredentialId.class, UserCredentialIdTypeHandler.class);
            factoryBean.setConfiguration(configuration);
            return factoryBean.getObject();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new TestPasswordEncoder();
        }

        @Bean
        UpmsPermissionCacheSupport upmsPermissionCacheSupport() {
            return new UpmsPermissionCacheSupport(
                    this.<TenantId, Long>buildCache(),
                    this.<String, Long>buildCache(),
                    this.<String, List<Menu>>buildCache(),
                    this.<String, List<Menu>>buildCache(),
                    this.<String, Set<String>>buildCache(),
                    this.<String, Set<DepartmentId>>buildCache(),
                    this.<String, Set<String>>buildCache());
        }

        private <K, V> Cache<K, V> buildCache() {
            return LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                    .limit(TEST_CACHE_LIMIT)
                    .buildCache();
        }

        @Bean
        TenantPersistenceSupport tenantPersistenceSupport(
                com.github.thundax.bacon.upms.infra.persistence.mapper.TenantMapper tenantMapper) {
            return new TenantPersistenceSupport(tenantMapper);
        }

        @Bean
        UserPersistenceSupport userPersistenceSupport(
                UserMapper userMapper) {
            return new UserPersistenceSupport(userMapper);
        }

        @Bean
        UserIdentityPersistenceSupport userIdentityPersistenceSupport(UserIdentityMapper userIdentityMapper) {
            return new UserIdentityPersistenceSupport(userIdentityMapper);
        }

        @Bean
        UserCredentialPersistenceSupport userCredentialPersistenceSupport(UserCredentialMapper userCredentialMapper) {
            return new UserCredentialPersistenceSupport(userCredentialMapper);
        }

        @Bean
        UserRolePersistenceSupport userRolePersistenceSupport(
                UserRoleRelMapper userRoleRelMapper, IdGenerator idGenerator) {
            return new UserRolePersistenceSupport(userRoleRelMapper, idGenerator);
        }

        @Bean
        DepartmentPersistenceSupport departmentPersistenceSupport(DepartmentMapper departmentMapper) {
            return new DepartmentPersistenceSupport(departmentMapper);
        }

        @Bean
        PostPersistenceSupport postPersistenceSupport(
                com.github.thundax.bacon.upms.infra.persistence.mapper.PostMapper postMapper) {
            return new PostPersistenceSupport(postMapper);
        }

        @Bean
        RolePersistenceSupport rolePersistenceSupport(RoleMapper roleMapper, UserRoleRelMapper userRoleRelMapper) {
            return new RolePersistenceSupport(roleMapper, userRoleRelMapper);
        }

        @Bean
        RoleMenuPersistenceSupport roleMenuPersistenceSupport(
                RoleMenuRelMapper roleMenuRelMapper, IdGenerator idGenerator) {
            return new RoleMenuPersistenceSupport(roleMenuRelMapper, idGenerator);
        }

        @Bean
        RoleResourcePersistenceSupport roleResourcePersistenceSupport(
                ResourceMapper resourceMapper, RoleResourceRelMapper roleResourceRelMapper, IdGenerator idGenerator) {
            return new RoleResourcePersistenceSupport(resourceMapper, roleResourceRelMapper, idGenerator);
        }

        @Bean
        RoleDataScopePersistenceSupport roleDataScopePersistenceSupport(
                DataPermissionRuleMapper dataPermissionRuleMapper,
                RoleDataScopeRelMapper roleDataScopeRelMapper,
                IdGenerator idGenerator) {
            return new RoleDataScopePersistenceSupport(dataPermissionRuleMapper, roleDataScopeRelMapper, idGenerator);
        }

        @Bean
        MenuPersistenceSupport menuPersistenceSupport(
                MenuMapper menuMapper, RoleMenuRelMapper roleMenuRelMapper) {
            return new MenuPersistenceSupport(menuMapper, roleMenuRelMapper);
        }

        @Bean
        ResourcePersistenceSupport resourcePersistenceSupport(
                ResourceMapper resourceMapper, RoleResourceRelMapper roleResourceRelMapper) {
            return new ResourcePersistenceSupport(resourceMapper, roleResourceRelMapper);
        }

        @Bean
        RoleRepositoryImpl roleRepository(
                RolePersistenceSupport rolePersistenceSupport,
                RoleDataScopePersistenceSupport roleDataScopePersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport) {
            return new RoleRepositoryImpl(
                    rolePersistenceSupport, roleDataScopePersistenceSupport, upmsPermissionCacheSupport);
        }

        @Bean
        RoleMenuRepository roleMenuRepository(
                RolePersistenceSupport rolePersistenceSupport,
                RoleMenuPersistenceSupport roleMenuPersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport) {
            return new RoleMenuRepositoryImpl(rolePersistenceSupport, roleMenuPersistenceSupport, upmsPermissionCacheSupport);
        }

        @Bean
        RoleResourceRepository roleResourceRepository(
                RolePersistenceSupport rolePersistenceSupport,
                RoleResourcePersistenceSupport roleResourcePersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport) {
            return new RoleResourceRepositoryImpl(
                    rolePersistenceSupport, roleResourcePersistenceSupport, upmsPermissionCacheSupport);
        }

        @Bean
        RoleDataScopeRepository roleDataScopeRepository(
                RolePersistenceSupport rolePersistenceSupport,
                RoleDataScopePersistenceSupport roleDataScopePersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport) {
            return new RoleDataScopeRepositoryImpl(
                    rolePersistenceSupport, roleDataScopePersistenceSupport, upmsPermissionCacheSupport);
        }

        @Bean
        ResourceRepository resourceRepository(
                ResourcePersistenceSupport resourcePersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport) {
            return new ResourceRepositoryImpl(resourcePersistenceSupport, upmsPermissionCacheSupport);
        }

        @Bean
        DepartmentRepository departmentRepository(
                DepartmentPersistenceSupport departmentPersistenceSupport, UserRepository userRepository) {
            return new DepartmentRepositoryImpl(departmentPersistenceSupport, userRepository);
        }

        @Bean
        UserRepositoryImpl userRepositoryImpl(
                UserPersistenceSupport userPersistenceSupport,
                UserIdentityPersistenceSupport userIdentityPersistenceSupport,
                UserCredentialPersistenceSupport userCredentialPersistenceSupport,
                UserRolePersistenceSupport userRolePersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport,
                Ids ids,
                IdGenerator idGenerator) {
            return new UserRepositoryImpl(
                    userPersistenceSupport,
                    userIdentityPersistenceSupport,
                    userCredentialPersistenceSupport,
                    userRolePersistenceSupport,
                    upmsPermissionCacheSupport);
        }

        @Bean
        UserIdentityRepository userIdentityRepository(UserIdentityPersistenceSupport userIdentityPersistenceSupport) {
            return new UserIdentityRepositoryImpl(userIdentityPersistenceSupport);
        }

        @Bean
        UserCredentialRepository userCredentialRepository(
                UserCredentialPersistenceSupport userCredentialPersistenceSupport) {
            return new UserCredentialRepositoryImpl(userCredentialPersistenceSupport);
        }

        @Bean
        UserRoleRepository userRoleRepository(
                UserRolePersistenceSupport userRolePersistenceSupport, RoleRepository roleRepository) {
            return new UserRoleRepositoryImpl(userRolePersistenceSupport, roleRepository);
        }

        @Bean
        IdGenerator idGenerator() {
            AtomicLong sequence = new AtomicLong(100L);
            return bizTag -> sequence.incrementAndGet();
        }

        @Bean
        Ids ids(IdGenerator idGenerator) {
            return new DefaultIds(idGenerator);
        }

        @Bean
        PermissionRepository permissionRepository(
                MenuRepository menuRepository,
                RoleRepository roleRepository,
                RoleMenuRepository roleMenuRepository,
                RoleResourceRepository roleResourceRepository,
                RoleDataScopeRepository roleDataScopeRepository,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport) {
            return new PermissionRepositoryImpl(
                    menuRepository,
                    roleRepository,
                    roleMenuRepository,
                    roleResourceRepository,
                    roleDataScopeRepository,
                    upmsPermissionCacheSupport);
        }

        @Bean
        MenuRepositoryImpl menuRepositoryImpl(
                MenuPersistenceSupport menuPersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport,
                IdGenerator idGenerator) {
            return new MenuRepositoryImpl(menuPersistenceSupport, upmsPermissionCacheSupport);
        }
    }

    private static final class TestPasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return "{noop}" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword.equals(encode(rawPassword));
        }
    }
}
