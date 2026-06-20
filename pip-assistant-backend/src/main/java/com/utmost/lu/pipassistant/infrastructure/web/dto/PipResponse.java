package com.utmost.lu.pipassistant.infrastructure.web.dto;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipStatus;

/**
 * API representation of a PIP. Dates are intentionally omitted until they become editable.
 */
public record PipResponse(Long id, String code, PipStatus status) {

    public static PipResponse from(Pip pip) {
        return new PipResponse(pip.id(), pip.code().value(), pip.status());
    }
}
