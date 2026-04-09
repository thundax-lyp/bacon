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
import com.github.thundax.bacon.common.id.core.DefaultIds;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
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
    private final RoleRepository roleRepository = CONTEXT.getBean(RoleRepository.class);
    private final MenuRepository menuRepository = CONTEXT.getBean(MenuRepository.class);
    private final ResourceRepository resourceRepository = CONTEXT.getBean(ResourceRepository.class);
    private final PermissionRepository permissionRepository = CONTEXT.getBean(PermissionRepository.class);
    private final DepartmentRepository departmentRepository = CONTEXT.getBean(DepartmentRepository.class);

    @BeforeEach
    void setUpSchema() throws Exception {
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
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
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
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
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
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
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
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
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
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
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
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
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
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
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

    @AfterAll
    static void closeContext() {
        CONTEXT.close();
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
        Department rootDepartment = departmentRepository.save(new Department(
                HEADQUARTERS_DEPARTMENT_ID,
                TENANT_ID,
                "ROOT",
                "Headquarters",
                null,
                null,
                1,
                DepartmentStatus.ENABLED,
                null,
                null,
                null,
                null));
        Department childDepartment = departmentRepository.save(new Department(
                OPERATIONS_DEPARTMENT_ID,
                TENANT_ID,
                "OPS",
                "Operations",
                rootDepartment.getId(),
                null,
                2,
                DepartmentStatus.ENABLED,
                null,
                null,
                null,
                null));
        Menu rootMenu = menuRepository.save(new Menu(
                null, TENANT_ID, "MENU", "System", null, "/system", "SystemPage", "shield", 1, null, List.of()));
        Menu childMenu = menuRepository.save(new Menu(
                null,
                TENANT_ID,
                "MENU",
                "Users",
                rootMenu.getId(),
                "/system/users",
                "UserPage",
                "user",
                2,
                "upms:user:view",
                List.of()));
        Resource resource = resourceRepository.save(new Resource(
                null,
                TENANT_ID,
                "upms:user:edit",
                "Edit User",
                ResourceType.API,
                "POST",
                "/users",
                ResourceStatus.ENABLED,
                null,
                null,
                null,
                null));
        Role role = roleRepository.save(new Role(
                null,
                TENANT_ID,
                "ADMIN",
                "Administrator",
                RoleType.SYSTEM_ROLE,
                RoleDataScopeType.SELF,
                RoleStatus.ENABLED,
                null,
                null,
                null,
                null));
        User user = userRepository.save(
                new User(
                        null,
                        TENANT_ID,
                        "Alice",
                        StoredObjectId.of(901L),
                        childDepartment.getId(),
                        UserStatus.ENABLED,
                        null,
                        null,
                        null,
                        null),
                "alice",
                "13800000001");

        roleRepository.assignMenus(TENANT_ID, role.getId(), Set.of(rootMenu.getId(), childMenu.getId()));
        roleRepository.assignResources(TENANT_ID, role.getId(), Set.of(resource.getCode()));
        roleRepository.assignDataScope(
                TENANT_ID,
                role.getId(),
                RoleDataScopeType.CUSTOM,
                Set.of(rootDepartment.getId(), childDepartment.getId()));
        userRepository.assignRoles(TENANT_ID, user.getId(), List.of(role.getId()));

        assertEquals(1, permissionRepository.listMenus(TENANT_ID).size());

        User persistedUser =
                userRepository.findUserByAccount(TENANT_ID, "alice").orElseThrow();
        assertNotNull(persistedUser.getId());
        assertTrue(persistedUser.getId().value() > 0);
        assertEquals(StoredObjectId.of(901L), persistedUser.getAvatarObjectId());
        assertTrue(userRepository
                .findUserIdentity(TENANT_ID, UserIdentityType.ACCOUNT, "alice")
                .isPresent());
        assertNotNull(userRepository
                .findUserCredential(TENANT_ID, persistedUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue());
        assertTrue(userRepository
                .findUserCredential(TENANT_ID, persistedUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .isNeedChangePassword());
        assertTrue(userRepository
                .findUserIdentity(TENANT_ID, UserIdentityType.PHONE, "13800000001")
                .isPresent());
        assertEquals(1L, userRepository.countUsers(TENANT_ID, "ali", null, null, "ENABLED"));

        List<Menu> menuTree = permissionRepository.getUserMenuTree(TENANT_ID, user.getId());
        assertEquals(1, menuTree.size());
        assertEquals(rootMenu.getId(), menuTree.get(0).getId());
        assertEquals(1, menuTree.get(0).getChildren().size());
        assertEquals(childMenu.getId(), menuTree.get(0).getChildren().get(0).getId());

        Set<String> permissionCodes = permissionRepository.getUserPermissionCodes(TENANT_ID, user.getId());
        assertTrue(permissionCodes.contains("upms:user:view"));
        assertTrue(permissionCodes.contains("upms:user:edit"));
        assertEquals(Set.of("CUSTOM"), permissionRepository.getUserScopeTypes(TENANT_ID, user.getId()));
        assertEquals(
                Set.of(rootDepartment.getId(), childDepartment.getId()),
                permissionRepository.getUserDepartmentIds(TENANT_ID, user.getId()));
        assertFalse(permissionRepository.hasAllAccess(TENANT_ID, user.getId()));
    }

    @Test
    void shouldReplacePhoneIdentityAndClearUserAssignmentsOnDelete() {
        Department department = departmentRepository.save(new Department(
                OPERATIONS_DEPARTMENT_ID,
                TENANT_ID,
                "OPS",
                "Operations",
                null,
                null,
                1,
                DepartmentStatus.ENABLED,
                null,
                null,
                null,
                null));
        Role role = roleRepository.save(new Role(
                null,
                TENANT_ID,
                "OPS_ADMIN",
                "Ops Admin",
                RoleType.SYSTEM_ROLE,
                RoleDataScopeType.SELF,
                RoleStatus.ENABLED,
                null,
                null,
                null,
                null));
        User createdUser = userRepository.save(
                new User(
                        null,
                        TENANT_ID,
                        "Bob",
                        StoredObjectId.of(1001L),
                        department.getId(),
                        UserStatus.ENABLED,
                        null,
                        null,
                        null,
                        null),
                "bob",
                "13800000002");

        userRepository.assignRoles(TENANT_ID, createdUser.getId(), List.of(role.getId()));
        User updatedUser = userRepository.save(
                new User(
                        createdUser.getId(),
                        TENANT_ID,
                        "Bob",
                        StoredObjectId.of(1002L),
                        department.getId(),
                        UserStatus.ENABLED,
                        null,
                        null,
                        null,
                        null),
                "bob",
                "13900000003");

        assertFalse(userRepository
                .findUserIdentity(TENANT_ID, UserIdentityType.PHONE, "13800000002")
                .isPresent());
        assertTrue(userRepository
                .findUserIdentity(TENANT_ID, UserIdentityType.PHONE, "13900000003")
                .isPresent());
        assertNotNull(userRepository
                .findUserCredential(TENANT_ID, updatedUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue());
        assertEquals(
                StoredObjectId.of(1002L),
                userRepository
                        .findUserById(TENANT_ID, updatedUser.getId())
                        .orElseThrow()
                        .getAvatarObjectId());
        assertTrue(departmentRepository.existsUserInDepartment(TENANT_ID, department.getId()));

        userRepository.deleteUser(TENANT_ID, updatedUser.getId());

        assertFalse(userRepository.findUserById(TENANT_ID, updatedUser.getId()).isPresent());
        assertFalse(userRepository
                .findUserIdentity(TENANT_ID, UserIdentityType.ACCOUNT, "bob")
                .isPresent());
        assertTrue(
                roleRepository.findRolesByUserId(TENANT_ID, updatedUser.getId()).isEmpty());
        assertFalse(departmentRepository.existsUserInDepartment(TENANT_ID, department.getId()));
        assertTrue(isUserDeleted(String.valueOf(updatedUser.getId().value())));
    }

    @Test
    void shouldSyncAccountIdentityPasswordWhenUpdatingPassword() {
        Department department = departmentRepository.save(new Department(
                OPERATIONS_DEPARTMENT_ID,
                TENANT_ID,
                "OPS",
                "Operations",
                null,
                null,
                1,
                DepartmentStatus.ENABLED,
                null,
                null,
                null,
                null));
        User createdUser = userRepository.save(
                new User(
                        null, TENANT_ID, "Carol", null, department.getId(), UserStatus.ENABLED, null, null, null, null),
                "carol",
                "13600000001");

        String originalPasswordHash = userRepository
                .findUserCredential(TENANT_ID, createdUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue();

        User updatedUser = userRepository.updatePassword(TENANT_ID, createdUser.getId(), "654321", false);

        String updatedPasswordHash = userRepository
                .findUserCredential(TENANT_ID, createdUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .getCredentialValue();
        assertNotEquals(originalPasswordHash, updatedPasswordHash);
        assertFalse(userRepository
                .findUserCredential(TENANT_ID, createdUser.getId(), UserCredentialType.PASSWORD)
                .orElseThrow()
                .isNeedChangePassword());
    }

    @Test
    void shouldReplaceRoleRelationsAndSupportDepartmentHierarchyQueries() {
        Department root = departmentRepository.save(new Department(
                HEADQUARTERS_DEPARTMENT_ID,
                TENANT_ID,
                "ROOT",
                "Root",
                null,
                null,
                1,
                DepartmentStatus.ENABLED,
                null,
                null,
                null,
                null));
        Department child = departmentRepository.save(new Department(
                CHILD_DEPARTMENT_ID,
                TENANT_ID,
                "CHILD",
                "Child",
                root.getId(),
                null,
                2,
                DepartmentStatus.ENABLED,
                null,
                null,
                null,
                null));
        Menu oldMenu = menuRepository.save(new Menu(
                null, TENANT_ID, "MENU", "Old", null, "/old", "OldPage", "archive", 1, "upms:old:view", List.of()));
        Menu newMenu = menuRepository.save(new Menu(
                null, TENANT_ID, "MENU", "New", null, "/new", "NewPage", "star", 2, "upms:new:view", List.of()));
        Resource oldResource = resourceRepository.save(new Resource(
                null,
                TENANT_ID,
                "upms:old:edit",
                "Old Edit",
                ResourceType.API,
                "POST",
                "/old",
                ResourceStatus.ENABLED,
                null,
                null,
                null,
                null));
        Resource newResource = resourceRepository.save(new Resource(
                null,
                TENANT_ID,
                "upms:new:edit",
                "New Edit",
                ResourceType.API,
                "PUT",
                "/new",
                ResourceStatus.ENABLED,
                null,
                null,
                null,
                null));
        Role role = roleRepository.save(new Role(
                null,
                TENANT_ID,
                "MANAGER",
                "Manager",
                RoleType.SYSTEM_ROLE,
                RoleDataScopeType.SELF,
                RoleStatus.ENABLED,
                null,
                null,
                null,
                null));

        roleRepository.assignMenus(TENANT_ID, role.getId(), Set.of(oldMenu.getId()));
        roleRepository.assignResources(TENANT_ID, role.getId(), Set.of(oldResource.getCode()));
        roleRepository.assignDataScope(TENANT_ID, role.getId(), RoleDataScopeType.CUSTOM, Set.of(root.getId()));

        User user = userRepository.save(
                new User(null, TENANT_ID, "Manager", null, child.getId(), UserStatus.ENABLED, null, null, null, null),
                "manager",
                "13700000001");
        userRepository.assignRoles(TENANT_ID, user.getId(), List.of(role.getId()));

        assertEquals(
                Set.of("upms:old:view", "upms:old:edit"),
                permissionRepository.getUserPermissionCodes(TENANT_ID, user.getId()));
        assertEquals(Set.of("CUSTOM"), permissionRepository.getUserScopeTypes(TENANT_ID, user.getId()));
        assertEquals(
                1, permissionRepository.getUserMenuTree(TENANT_ID, user.getId()).size());

        roleRepository.assignMenus(TENANT_ID, role.getId(), Set.of(newMenu.getId()));
        roleRepository.assignResources(TENANT_ID, role.getId(), Set.of(newResource.getCode()));
        roleRepository.assignDataScope(TENANT_ID, role.getId(), RoleDataScopeType.ALL, Set.of(child.getId()));

        assertEquals(Set.of(newMenu.getId()), roleRepository.getAssignedMenus(TENANT_ID, role.getId()));
        assertEquals(Set.of(newResource.getCode()), roleRepository.getAssignedResources(TENANT_ID, role.getId()));
        assertEquals("ALL", roleRepository.getAssignedDataScopeType(TENANT_ID, role.getId()));
        assertEquals(Set.of(child.getId()), roleRepository.getAssignedDataScopeDepartments(TENANT_ID, role.getId()));
        assertEquals(
                Set.of("upms:new:view", "upms:new:edit"),
                permissionRepository.getUserPermissionCodes(TENANT_ID, user.getId()));
        assertEquals(Set.of("ALL"), permissionRepository.getUserScopeTypes(TENANT_ID, user.getId()));
        assertTrue(departmentRepository.existsChildDepartment(TENANT_ID, root.getId()));
        assertEquals(
                Set.of(root.getId(), child.getId()),
                departmentRepository.listDepartmentsByIds(TENANT_ID, Set.of(root.getId(), child.getId())).stream()
                        .map(Department::getId)
                        .collect(java.util.stream.Collectors.toSet()));

        menuRepository.deleteMenu(TENANT_ID, newMenu.getId());
        resourceRepository.delete(TENANT_ID, newResource.getId());

        assertTrue(roleRepository.getAssignedMenus(TENANT_ID, role.getId()).isEmpty());
        assertTrue(roleRepository.getAssignedResources(TENANT_ID, role.getId()).isEmpty());
        assertTrue(permissionRepository
                .getUserPermissionCodes(TENANT_ID, user.getId())
                .isEmpty());
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
                UserMapper userMapper,
                UserIdentityMapper userIdentityMapper,
                UserCredentialMapper userCredentialMapper) {
            return new UserPersistenceSupport(userMapper, userIdentityMapper, userCredentialMapper);
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
        RolePersistenceSupport rolePersistenceSupport(
                RoleMapper roleMapper,
                ResourceMapper resourceMapper,
                UserRoleRelMapper userRoleRelMapper,
                RoleMenuRelMapper roleMenuRelMapper,
                RoleResourceRelMapper roleResourceRelMapper,
                DataPermissionRuleMapper dataPermissionRuleMapper,
                RoleDataScopeRelMapper roleDataScopeRelMapper) {
            return new RolePersistenceSupport(
                    roleMapper,
                    resourceMapper,
                    userRoleRelMapper,
                    roleMenuRelMapper,
                    roleResourceRelMapper,
                    dataPermissionRuleMapper,
                    roleDataScopeRelMapper);
        }

        @Bean
        MenuPersistenceSupport menuPersistenceSupport(MenuMapper menuMapper) {
            return new MenuPersistenceSupport(menuMapper);
        }

        @Bean
        ResourcePersistenceSupport resourcePersistenceSupport(
                ResourceMapper resourceMapper, RoleResourceRelMapper roleResourceRelMapper) {
            return new ResourcePersistenceSupport(resourceMapper, roleResourceRelMapper);
        }

        @Bean
        RoleRepositoryImpl roleRepository(
                RolePersistenceSupport rolePersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport,
                IdGenerator idGenerator) {
            return new RoleRepositoryImpl(rolePersistenceSupport, upmsPermissionCacheSupport, idGenerator);
        }

        @Bean
        ResourceRepository resourceRepository(
                ResourcePersistenceSupport resourcePersistenceSupport,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport,
                Ids ids) {
            return new ResourceRepositoryImpl(resourcePersistenceSupport, upmsPermissionCacheSupport, ids);
        }

        @Bean
        DepartmentRepository departmentRepository(
                DepartmentPersistenceSupport departmentPersistenceSupport, UserRepositoryImpl userRepository) {
            return new DepartmentRepositoryImpl(departmentPersistenceSupport, userRepository);
        }

        @Bean
        UserRepositoryImpl userRepositoryImpl(
                UserPersistenceSupport userPersistenceSupport,
                RoleRepositoryImpl roleRepository,
                PasswordEncoder passwordEncoder,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport,
                Ids ids,
                IdGenerator idGenerator) {
            return new UserRepositoryImpl(
                    userPersistenceSupport,
                    roleRepository,
                    passwordEncoder,
                    upmsPermissionCacheSupport,
                    ids,
                    idGenerator);
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
                MenuRepositoryImpl menuRepository,
                RoleRepositoryImpl roleRepository,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport) {
            return new PermissionRepositoryImpl(menuRepository, roleRepository, upmsPermissionCacheSupport);
        }

        @Bean
        MenuRepositoryImpl menuRepositoryImpl(
                MenuPersistenceSupport menuPersistenceSupport,
                RoleRepositoryImpl roleRepository,
                UpmsPermissionCacheSupport upmsPermissionCacheSupport,
                IdGenerator idGenerator) {
            return new MenuRepositoryImpl(
                    menuPersistenceSupport, roleRepository, upmsPermissionCacheSupport, idGenerator);
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
