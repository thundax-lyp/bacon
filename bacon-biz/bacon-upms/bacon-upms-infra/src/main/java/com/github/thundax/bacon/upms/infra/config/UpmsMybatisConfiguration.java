package com.github.thundax.bacon.upms.infra.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.handler.DepartmentIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.MenuIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.PostIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.RoleIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.UserCredentialIdTypeHandler;
import com.github.thundax.bacon.upms.infra.persistence.handler.UserIdentityIdTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("!test")
@MapperScan("com.github.thundax.bacon.upms.infra.persistence.mapper")
public class UpmsMybatisConfiguration {

    @Bean
    public ConfigurationCustomizer upmsTypeHandlerCustomizer() {
        return configuration -> registerUpmsTypeHandlers(configuration.getTypeHandlerRegistry());
    }

    private void registerUpmsTypeHandlers(TypeHandlerRegistry registry) {
        registry.register(DepartmentId.class, DepartmentIdTypeHandler.class);
        registry.register(MenuId.class, MenuIdTypeHandler.class);
        registry.register(PostId.class, PostIdTypeHandler.class);
        registry.register(RoleId.class, RoleIdTypeHandler.class);
        registry.register(UserIdentityId.class, UserIdentityIdTypeHandler.class);
        registry.register(UserCredentialId.class, UserCredentialIdTypeHandler.class);
    }
}
