package com.utmost.lu.pipassistant.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** JIRA connection settings bound from {@code pip.jira.*} in application config. */
@Component
@ConfigurationProperties(prefix = "pip.jira")
public class JiraProperties {

    private String baseUrl = "";
    private String apiToken = "";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }
}
