package com.cyancoder.platformopenapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("platform.openapi")
public class PlatformOpenApiProperties {
    private boolean enabled = true;
    private String title;
    private String description = "Controller-derived platform API";
    private String version = "1.0.0";
    private String serverUrl;
    private PlatformApiSecurity defaultSecurity = PlatformApiSecurity.BEARER;
    private DocsAccess docsAccess = DocsAccess.BASIC;
    private String docsUsername;
    private String docsPassword;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public PlatformApiSecurity getDefaultSecurity() {
        return defaultSecurity;
    }

    public void setDefaultSecurity(PlatformApiSecurity defaultSecurity) {
        this.defaultSecurity = defaultSecurity;
    }

    public DocsAccess getDocsAccess() {
        return docsAccess;
    }

    public void setDocsAccess(DocsAccess docsAccess) {
        this.docsAccess = docsAccess;
    }

    public String getDocsUsername() {
        return docsUsername;
    }

    public void setDocsUsername(String docsUsername) {
        this.docsUsername = docsUsername;
    }

    public String getDocsPassword() {
        return docsPassword;
    }

    public void setDocsPassword(String docsPassword) {
        this.docsPassword = docsPassword;
    }

    public enum DocsAccess {
        BASIC,
        PUBLIC,
        DISABLED
    }
}
