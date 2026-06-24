package com.utmost.lu.pipassistant.application;

import java.util.List;

/**
 * Source of the allowed requirement statuses (configurable). Implemented by an
 * infrastructure adapter backed by application configuration.
 */
public interface RequirementStatusCatalog {

    List<String> all();
}
