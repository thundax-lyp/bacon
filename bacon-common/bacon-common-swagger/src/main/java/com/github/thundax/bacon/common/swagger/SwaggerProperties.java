package com.github.thundax.bacon.common.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bacon.swagger")
public class SwaggerProperties {

    private final Oauth2 oauth2 = new Oauth2();

    public Oauth2 getOauth2() {
        return oauth2;
    }

    public static class Oauth2 {

        private boolean enabled = true;
        private String authorizationUrl;
        private String tokenUrl;
        private String refreshUrl;
        private String authorizationPath = "/auth/oauth2/authorize";
        private String tokenPath = "/auth/oauth2/token";
        private String refreshPath = "/token/refresh";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAuthorizationUrl() {
            return authorizationUrl;
        }

        public void setAuthorizationUrl(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getRefreshUrl() {
            return refreshUrl;
        }

        public void setRefreshUrl(String refreshUrl) {
            this.refreshUrl = refreshUrl;
        }

        public String getAuthorizationPath() {
            return authorizationPath;
        }

        public void setAuthorizationPath(String authorizationPath) {
            this.authorizationPath = authorizationPath;
        }

        public String getTokenPath() {
            return tokenPath;
        }

        public void setTokenPath(String tokenPath) {
            this.tokenPath = tokenPath;
        }

        public String getRefreshPath() {
            return refreshPath;
        }

        public void setRefreshPath(String refreshPath) {
            this.refreshPath = refreshPath;
        }
    }
}
