package com.github.thundax.bacon.boot;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BaconMonoPersistenceSupportConfiguration {

    @Bean
    @ConditionalOnMissingBean(SqlSessionFactory.class)
    SqlSessionFactory sqlSessionFactory(DataSource dataSource,
                                        ObjectProvider<MybatisPlusInterceptor> mybatisPlusInterceptor,
                                        ObjectProvider<MetaObjectHandler> metaObjectHandler) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTypeHandlersPackage("com.github.thundax.bacon.common.mybatis.handler");

        MybatisPlusInterceptor interceptor = mybatisPlusInterceptor.getIfAvailable();
        if (interceptor != null) {
            factoryBean.setPlugins(interceptor);
        }

        MetaObjectHandler handler = metaObjectHandler.getIfAvailable();
        if (handler != null) {
            GlobalConfig globalConfig = new GlobalConfig();
            globalConfig.setMetaObjectHandler(handler);
            factoryBean.setGlobalConfig(globalConfig);
        }
        return factoryBean.getObject();
    }

    @Bean
    @ConditionalOnMissingBean(SqlSessionTemplate.class)
    SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
