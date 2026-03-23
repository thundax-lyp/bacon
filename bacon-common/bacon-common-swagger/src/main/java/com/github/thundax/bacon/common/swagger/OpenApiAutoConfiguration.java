package com.github.thundax.bacon.common.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SwaggerProperties.class)
public class OpenApiAutoConfiguration {

    @Bean
    public OpenAPI baconOpenApi(Environment environment, SwaggerProperties swaggerProperties) {
        if (!swaggerProperties.getOauth2().isEnabled()) {
            return new OpenAPI().info(new Info().title("Bacon API").version("v1"));
        }

        String authorizationUrl = resolveUrl(environment, swaggerProperties.getOauth2().getAuthorizationUrl(),
                swaggerProperties.getOauth2().getAuthorizationPath());
        String tokenUrl = resolveUrl(environment, swaggerProperties.getOauth2().getTokenUrl(),
                swaggerProperties.getOauth2().getTokenPath());
        String refreshUrl = resolveUrl(environment, swaggerProperties.getOauth2().getRefreshUrl(),
                swaggerProperties.getOauth2().getRefreshPath());

        Scopes scopes = new Scopes()
                .addString("openid", "OpenID scope")
                .addString("profile", "Profile scope");

        SecurityScheme oauth2Scheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                        .authorizationUrl(authorizationUrl)
                        .tokenUrl(tokenUrl)
                        .refreshUrl(refreshUrl)
                        .scopes(scopes)));

        return new OpenAPI()
                .info(new Info().title("Bacon API").version("v1"))
                .components(new Components().addSecuritySchemes("oauth2", oauth2Scheme))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"));
    }

    private String resolveUrl(Environment environment, String explicitUrl, String path) {
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            return explicitUrl;
        }

        String runtimeMode = environment.getProperty("bacon.runtime.mode", "mono");
        if ("micro".equalsIgnoreCase(runtimeMode)) {
            String authBaseUrl = environment.getProperty("bacon.remote.auth-base-url", "http://localhost:8081/api");
            return joinUrl(authBaseUrl, path);
        }

        String contextPath = environment.getProperty("server.servlet.context-path", "");
        return joinUrl(contextPath, path);
    }

    private String joinUrl(String base, String path) {
        String normalizedBase = base == null ? "" : base.trim();
        String normalizedPath = path == null ? "" : path.trim();

        if (normalizedBase.endsWith("/") && normalizedPath.startsWith("/")) {
            return normalizedBase.substring(0, normalizedBase.length() - 1) + normalizedPath;
        }
        if (!normalizedBase.endsWith("/") && !normalizedBase.isEmpty() && !normalizedPath.startsWith("/")) {
            return normalizedBase + "/" + normalizedPath;
        }
        return normalizedBase + normalizedPath;
    }
}
