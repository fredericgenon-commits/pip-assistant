package com.utmost.lu.pipassistant.infrastructure.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.application.RequirementStatusCatalog;

/**
 * Requirement statuses bound from {@code pip.requirement.statuses} in application config.
 */
@Component
@ConfigurationProperties(prefix = "pip.requirement")
public class RequirementStatusProperties implements RequirementStatusCatalog {

    private List<String> statuses = new ArrayList<>();

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    @Override
    public List<String> all() {
        return List.copyOf(statuses);
    }
}
