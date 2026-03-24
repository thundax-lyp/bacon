package com.github.thundax.bacon.common.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.context.event.EventListener;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

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

    @EventListener(ApplicationReadyEvent.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public void logSwaggerEndpoints(ApplicationReadyEvent event) {
        Environment environment = event.getApplicationContext().getEnvironment();
        String baseUrl = buildBaseUrl(environment);
        String swaggerUiUrl = joinUrl(baseUrl, "/swagger-ui/index.html");
        String apiDocsUrl = joinUrl(baseUrl, "/v3/api-docs");

        log.info("Swagger UI: {}", swaggerUiUrl);
        log.info("OpenAPI Docs: {}", apiDocsUrl);
    }

    private String resolveUrl(Environment environment, String explicitUrl, String path) {
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            return explicitUrl;
        }

        String runtimeMode = environment.getProperty("bacon.runtime.mode", "mono");
        if ("micro".equalsIgnoreCase(runtimeMode)) {
            String authBaseUrl = environment.getProperty("bacon.remote.auth-base-url", "http://127.0.0.1:8081/api");
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

    private String buildBaseUrl(Environment environment) {
        String scheme = Boolean.parseBoolean(environment.getProperty("server.ssl.enabled", "false")) ? "https" : "http";
        String host = environment.getProperty("server.address");
        if (host == null || host.isBlank() || "0.0.0.0".equals(host)) {
            host = "127.0.0.1";
        }

        String port = environment.getProperty("local.server.port");
        if (port == null || port.isBlank()) {
            port = environment.getProperty("server.port", "8080");
        }

        String contextPath = environment.getProperty("server.servlet.context-path", "");
        return scheme + "://" + host + ":" + port + contextPath;
    }
}
