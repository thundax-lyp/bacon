package com.github.thundax.bacon.boot;

import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.domain.repository.PostRepository;
import com.github.thundax.bacon.upms.domain.repository.ResourceRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleDataScopeRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleMenuRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleResourceRepository;
import com.github.thundax.bacon.upms.domain.repository.SysLogRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRoleRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class UpmsRepositoryTestConfiguration {

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public UserIdentityRepository userIdentityRepository() {
        return Mockito.mock(UserIdentityRepository.class);
    }

    @Bean
    @Primary
    public UserCredentialRepository userCredentialRepository() {
        return Mockito.mock(UserCredentialRepository.class);
    }

    @Bean
    @Primary
    public UserRoleRepository userRoleRepository() {
        return Mockito.mock(UserRoleRepository.class);
    }

    @Bean
    @Primary
    public DepartmentRepository departmentRepository() {
        return Mockito.mock(DepartmentRepository.class);
    }

    @Bean
    @Primary
    public RoleRepository roleRepository() {
        return Mockito.mock(RoleRepository.class);
    }

    @Bean
    @Primary
    public RoleMenuRepository roleMenuRepository() {
        return Mockito.mock(RoleMenuRepository.class);
    }

    @Bean
    @Primary
    public RoleResourceRepository roleResourceRepository() {
        return Mockito.mock(RoleResourceRepository.class);
    }

    @Bean
    @Primary
    public RoleDataScopeRepository roleDataScopeRepository() {
        return Mockito.mock(RoleDataScopeRepository.class);
    }

    @Bean
    @Primary
    public PermissionRepository permissionRepository() {
        return Mockito.mock(PermissionRepository.class);
    }

    @Bean
    @Primary
    public TenantRepository tenantRepository() {
        return Mockito.mock(TenantRepository.class);
    }

    @Bean
    @Primary
    public MenuRepository menuRepository() {
        return Mockito.mock(MenuRepository.class);
    }

    @Bean
    @Primary
    public PostRepository postRepository() {
        return Mockito.mock(PostRepository.class);
    }

    @Bean
    @Primary
    public ResourceRepository resourceRepository() {
        return Mockito.mock(ResourceRepository.class);
    }

    @Bean
    @Primary
    public SysLogRepository sysLogRepository() {
        return Mockito.mock(SysLogRepository.class);
    }

    @Bean
    @Primary
    public StoredObjectCommandFacade storedObjectCommandFacade() {
        return Mockito.mock(StoredObjectCommandFacade.class);
    }

    @Bean
    @Primary
    public StoredObjectReadFacade storedObjectReadFacade() {
        return Mockito.mock(StoredObjectReadFacade.class);
    }
}
