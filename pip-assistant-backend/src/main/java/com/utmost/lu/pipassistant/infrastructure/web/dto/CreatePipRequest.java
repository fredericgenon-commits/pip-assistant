package com.utmost.lu.pipassistant.infrastructure.web.dto;

/**
 * Request body to create a PIP. The code is validated by the domain
 * ({@code PipCode}); a null/blank/invalid value yields HTTP 400.
 */
public record CreatePipRequest(String code) {
}
