package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Resource;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DataPermissionRuleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.DepartmentMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.MenuMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.ResourceMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleDataScopeRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMenuRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleResourceRelMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpmsRepositoryIntegrationTest {

    private static final AnnotationConfigApplicationContext CONTEXT =
            new AnnotationConfigApplicationContext(TestConfig.class);

    private final DataSource dataSource = CONTEXT.getBean(DataSource.class);
    private final UserRepository userRepository = CONTEXT.getBean(UserRepository.class);
    private final RoleRepository roleRepository = CONTEXT.getBean(RoleRepository.class);
    private final MenuRepository menuRepository = CONTEXT.getBean(MenuRepository.class);
    private final ResourceRepository resourceRepository = CONTEXT.getBean(ResourceRepository.class);
    private final PermissionRepository permissionRepository = CONTEXT.getBean(PermissionRepository.class);
    private final DepartmentRepository departmentRepository = CONTEXT.getBean(DepartmentRepository.class);

    @BeforeEach
    void setUpSchema() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role_data_scope_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_data_permission_rule");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role_resource_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role_menu_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_user_role_rel");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_user_identity");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_resource");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_menu");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_role");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_user");
            statement.execute("DROP TABLE IF EXISTS bacon_upms_department");

            statement.execute("""
                    CREATE TABLE bacon_upms_department (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        code varchar(64) NOT NULL,
                        name varchar(128) NOT NULL,
                        parent_id bigint NULL,
                        leader_user_id bigint NULL,
                        status varchar(16) NOT NULL,
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_user (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        account varchar(64) NOT NULL,
                        name varchar(128) NOT NULL,
                        phone varchar(32) NULL,
                        password_hash varchar(255) NOT NULL,
                        department_id bigint NULL,
                        status varchar(16) NOT NULL,
                        deleted boolean NOT NULL,
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_user_identity (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        user_id bigint NOT NULL,
                        identity_type varchar(32) NOT NULL,
                        identity_value varchar(128) NOT NULL,
                        enabled boolean NOT NULL,
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_role (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
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
            statement.execute("""
                    CREATE TABLE bacon_upms_menu (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        menu_type varchar(32) NOT NULL,
                        name varchar(128) NOT NULL,
                        parent_id bigint NULL,
                        route_path varchar(255) NULL,
                        component_name varchar(255) NULL,
                        icon varchar(128) NULL,
                        sort int NOT NULL,
                        permission_code varchar(128) NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_resource (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
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
            statement.execute("""
                    CREATE TABLE bacon_upms_user_role_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        user_id bigint NOT NULL,
                        role_id bigint NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_role_menu_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        role_id bigint NOT NULL,
                        menu_id bigint NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_role_resource_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        role_id bigint NOT NULL,
                        resource_id bigint NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_data_permission_rule (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        role_id bigint NOT NULL,
                        data_scope_type varchar(32) NOT NULL,
                        created_by varchar(64) NULL,
                        created_at timestamp NULL,
                        updated_by varchar(64) NULL,
                        updated_at timestamp NULL,
                        PRIMARY KEY (id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE bacon_upms_role_data_scope_rel (
                        id bigint NOT NULL AUTO_INCREMENT,
                        tenant_id bigint NOT NULL,
                        role_id bigint NOT NULL,
                        department_id bigint NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
        }
    }

    @AfterAll
    static void closeContext() {
        CONTEXT.close();
    }

    @Test
    void shouldPersistUserRoleAndPermissionGraph() {
        Department rootDepartment = departmentRepository.save(new Department(null, 1001L, "ROOT", "Headquarters", 0L, null, "ACTIVE"));
        Department childDepartment = departmentRepository.save(new Department(null, 1001L, "OPS", "Operations", rootDepartment.getId(), null, "ACTIVE"));
        Menu rootMenu = menuRepository.save(new Menu(null, 1001L, "MENU", "System", 0L, "/system", "SystemPage", "shield", 1, null, List.of()));
        Menu childMenu = menuRepository.save(new Menu(null, 1001L, "MENU", "Users", rootMenu.getId(), "/system/users", "UserPage", "user", 2, "upms:user:view", List.of()));
        Resource resource = resourceRepository.save(new Resource(null, 1001L, "upms:user:edit", "Edit User", "API", "POST", "/users", "ACTIVE"));
        Role role = roleRepository.save(new Role(null, 1001L, "ADMIN", "Administrator", "SYSTEM", "SELF", "ACTIVE"));
        User user = userRepository.save(new User(null, 1001L, "alice", "Alice", "13800000001", null, childDepartment.getId(), "ACTIVE", false));

        roleRepository.assignMenus(1001L, role.getId(), Set.of(rootMenu.getId(), childMenu.getId()));
        roleRepository.assignResources(1001L, role.getId(), Set.of(resource.getCode()));
        roleRepository.assignDataScope(1001L, role.getId(), "CUSTOM", Set.of(rootDepartment.getId(), childDepartment.getId()));
        userRepository.assignRoles(1001L, user.getId(), List.of(role.getId()));

        User persistedUser = userRepository.findUserByAccount(1001L, "alice").orElseThrow();
        assertNotNull(persistedUser.getId());
        assertNotNull(persistedUser.getPasswordHash());
        assertTrue(userRepository.findUserIdentity(1001L, "ACCOUNT", "alice").isPresent());
        assertTrue(userRepository.findUserIdentity(1001L, "PHONE", "13800000001").isPresent());
        assertEquals(1L, userRepository.countUsers(1001L, "ali", null, null, "ACTIVE"));

        List<Menu> menuTree = permissionRepository.getUserMenuTree(1001L, user.getId());
        assertEquals(1, menuTree.size());
        assertEquals(rootMenu.getId(), menuTree.get(0).getId());
        assertEquals(1, menuTree.get(0).getChildren().size());
        assertEquals(childMenu.getId(), menuTree.get(0).getChildren().get(0).getId());

        Set<String> permissionCodes = permissionRepository.getUserPermissionCodes(1001L, user.getId());
        assertTrue(permissionCodes.contains("upms:user:view"));
        assertTrue(permissionCodes.contains("upms:user:edit"));
        assertEquals(Set.of("CUSTOM"), permissionRepository.getUserScopeTypes(1001L, user.getId()));
        assertEquals(Set.of(rootDepartment.getId(), childDepartment.getId()),
                permissionRepository.getUserDepartmentIds(1001L, user.getId()));
        assertFalse(permissionRepository.hasAllAccess(1001L, user.getId()));
    }

    @Test
    void shouldReplacePhoneIdentityAndClearUserAssignmentsOnDelete() {
        Department department = departmentRepository.save(new Department(null, 1001L, "OPS", "Operations", 0L, null, "ACTIVE"));
        Role role = roleRepository.save(new Role(null, 1001L, "OPS_ADMIN", "Ops Admin", "SYSTEM", "SELF", "ACTIVE"));
        User createdUser = userRepository.save(new User(null, 1001L, "bob", "Bob", "13800000002", null, department.getId(), "ACTIVE", false));

        userRepository.assignRoles(1001L, createdUser.getId(), List.of(role.getId()));
        User updatedUser = userRepository.save(new User(createdUser.getId(), 1001L, "bob", "Bob", "13900000003",
                createdUser.getPasswordHash(), department.getId(), "ACTIVE", false));

        assertFalse(userRepository.findUserIdentity(1001L, "PHONE", "13800000002").isPresent());
        assertTrue(userRepository.findUserIdentity(1001L, "PHONE", "13900000003").isPresent());
        assertTrue(departmentRepository.existsUserInDepartment(1001L, department.getId()));

        userRepository.deleteUser(1001L, updatedUser.getId());

        assertFalse(userRepository.findUserById(1001L, updatedUser.getId()).isPresent());
        assertFalse(userRepository.findUserIdentity(1001L, "ACCOUNT", "bob").isPresent());
        assertTrue(roleRepository.findRolesByUserId(1001L, updatedUser.getId()).isEmpty());
        assertFalse(departmentRepository.existsUserInDepartment(1001L, department.getId()));
    }

    @Test
    void shouldReplaceRoleRelationsAndSupportDepartmentHierarchyQueries() {
        Department root = departmentRepository.save(new Department(null, 1001L, "ROOT", "Root", 0L, null, "ACTIVE"));
        Department child = departmentRepository.save(new Department(null, 1001L, "CHILD", "Child", root.getId(), null, "ACTIVE"));
        Menu oldMenu = menuRepository.save(new Menu(null, 1001L, "MENU", "Old", 0L, "/old", "OldPage", "archive", 1, "upms:old:view", List.of()));
        Menu newMenu = menuRepository.save(new Menu(null, 1001L, "MENU", "New", 0L, "/new", "NewPage", "star", 2, "upms:new:view", List.of()));
        Resource oldResource = resourceRepository.save(new Resource(null, 1001L, "upms:old:edit", "Old Edit", "API", "POST", "/old", "ACTIVE"));
        Resource newResource = resourceRepository.save(new Resource(null, 1001L, "upms:new:edit", "New Edit", "API", "PUT", "/new", "ACTIVE"));
        Role role = roleRepository.save(new Role(null, 1001L, "MANAGER", "Manager", "SYSTEM", "SELF", "ACTIVE"));

        roleRepository.assignMenus(1001L, role.getId(), Set.of(oldMenu.getId()));
        roleRepository.assignResources(1001L, role.getId(), Set.of(oldResource.getCode()));
        roleRepository.assignDataScope(1001L, role.getId(), "CUSTOM", Set.of(root.getId()));

        roleRepository.assignMenus(1001L, role.getId(), Set.of(newMenu.getId()));
        roleRepository.assignResources(1001L, role.getId(), Set.of(newResource.getCode()));
        roleRepository.assignDataScope(1001L, role.getId(), "ALL", Set.of(child.getId()));

        assertEquals(Set.of(newMenu.getId()), roleRepository.getAssignedMenus(1001L, role.getId()));
        assertEquals(Set.of(newResource.getCode()), roleRepository.getAssignedResources(1001L, role.getId()));
        assertEquals("ALL", roleRepository.getAssignedDataScopeType(1001L, role.getId()));
        assertEquals(Set.of(child.getId()), roleRepository.getAssignedDataScopeDepartments(1001L, role.getId()));
        assertTrue(departmentRepository.existsChildDepartment(1001L, root.getId()));
        assertEquals(Set.of(root.getId(), child.getId()),
                departmentRepository.listDepartmentsByIds(1001L, Set.of(root.getId(), child.getId())).stream()
                        .map(Department::getId)
                        .collect(java.util.stream.Collectors.toSet()));

        menuRepository.deleteMenu(1001L, newMenu.getId());
        resourceRepository.delete(1001L, newResource.getId());

        assertTrue(roleRepository.getAssignedMenus(1001L, role.getId()).isEmpty());
        assertTrue(roleRepository.getAssignedResources(1001L, role.getId()).isEmpty());
        assertNotEquals(oldMenu.getId(), newMenu.getId());
        assertNotEquals(oldResource.getId(), newResource.getId());
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan("com.github.thundax.bacon.upms.infra.persistence.mapper")
    static class TestConfig {

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
            return factoryBean.getObject();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new PasswordEncoder() {
                @Override
                public String encode(CharSequence rawPassword) {
                    return "{noop}" + rawPassword;
                }

                @Override
                public boolean matches(CharSequence rawPassword, String encodedPassword) {
                    return encodedPassword.equals(encode(rawPassword));
                }
            };
        }

        @Bean
        TenantPersistenceSupport tenantPersistenceSupport(com.github.thundax.bacon.upms.infra.persistence.mapper.TenantMapper tenantMapper) {
            return new TenantPersistenceSupport(tenantMapper);
        }

        @Bean
        UserPersistenceSupport userPersistenceSupport(UserMapper userMapper, UserIdentityMapper userIdentityMapper) {
            return new UserPersistenceSupport(userMapper, userIdentityMapper);
        }

        @Bean
        DepartmentPersistenceSupport departmentPersistenceSupport(DepartmentMapper departmentMapper) {
            return new DepartmentPersistenceSupport(departmentMapper);
        }

        @Bean
        PostPersistenceSupport postPersistenceSupport(com.github.thundax.bacon.upms.infra.persistence.mapper.PostMapper postMapper) {
            return new PostPersistenceSupport(postMapper);
        }

        @Bean
        RolePersistenceSupport rolePersistenceSupport(RoleMapper roleMapper,
                                                     ResourceMapper resourceMapper,
                                                     UserRoleRelMapper userRoleRelMapper,
                                                     RoleMenuRelMapper roleMenuRelMapper,
                                                     RoleResourceRelMapper roleResourceRelMapper,
                                                     DataPermissionRuleMapper dataPermissionRuleMapper,
                                                     RoleDataScopeRelMapper roleDataScopeRelMapper) {
            return new RolePersistenceSupport(roleMapper, resourceMapper, userRoleRelMapper, roleMenuRelMapper,
                    roleResourceRelMapper, dataPermissionRuleMapper, roleDataScopeRelMapper);
        }

        @Bean
        MenuPersistenceSupport menuPersistenceSupport(MenuMapper menuMapper) {
            return new MenuPersistenceSupport(menuMapper);
        }

        @Bean
        ResourcePersistenceSupport resourcePersistenceSupport(ResourceMapper resourceMapper,
                                                             RoleResourceRelMapper roleResourceRelMapper) {
            return new ResourcePersistenceSupport(resourceMapper, roleResourceRelMapper);
        }

        @Bean
        RoleRepositoryImpl roleRepository(RolePersistenceSupport rolePersistenceSupport) {
            return new RoleRepositoryImpl(rolePersistenceSupport);
        }

        @Bean
        ResourceRepository resourceRepository(ResourcePersistenceSupport resourcePersistenceSupport) {
            return new ResourceRepositoryImpl(resourcePersistenceSupport);
        }

        @Bean
        DepartmentRepository departmentRepository(DepartmentPersistenceSupport departmentPersistenceSupport,
                                                  UserRepositoryImpl userRepository) {
            return new DepartmentRepositoryImpl(departmentPersistenceSupport, userRepository);
        }

        @Bean
        UserRepositoryImpl userRepositoryImpl(UserPersistenceSupport userPersistenceSupport,
                                              RoleRepositoryImpl roleRepository,
                                              PasswordEncoder passwordEncoder) {
            return new UserRepositoryImpl(userPersistenceSupport, roleRepository, passwordEncoder);
        }

        @Bean
        PermissionRepository permissionRepository(MenuRepositoryImpl menuRepository, RoleRepositoryImpl roleRepository) {
            return new PermissionRepositoryImpl(menuRepository, roleRepository);
        }

        @Bean
        MenuRepositoryImpl menuRepositoryImpl(MenuPersistenceSupport menuPersistenceSupport, RoleRepositoryImpl roleRepository) {
            return new MenuRepositoryImpl(menuPersistenceSupport, roleRepository);
        }
    }
}
