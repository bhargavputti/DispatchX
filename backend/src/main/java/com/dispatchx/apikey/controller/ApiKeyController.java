package com.dispatchx.apikey.controller;

import com.dispatchx.apikey.cache.ApiKeyStatusCacheService;
import com.dispatchx.apikey.dto.ApiKeyCreatedResponse;
import com.dispatchx.apikey.dto.ApiKeyResponse;
import com.dispatchx.apikey.dto.CreateApiKeyRequest;
import com.dispatchx.apikey.dto.RotateApiKeyRequest;
import com.dispatchx.apikey.service.ApiKeyService;
import com.dispatchx.common.context.TenantContext;
import com.dispatchx.common.context.TenantPrincipal;
import com.dispatchx.common.error.ErrorCode;
import com.dispatchx.common.error.DispatchXException;
import com.dispatchx.common.model.TenantStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Tenant API key management endpoints.
 *
 * All endpoints require tenant owner JWT authentication (never API key auth).
 * Enforces tenant status:
 * - ACTIVE: full access
 * - SUSPENDED: GET permitted (read-only); POST/DELETE blocked with 403 TENANT_SUSPENDED
 * - REVOKED: blocked
 */
@RestController
@RequestMapping("/api/v1/keys")
@PreAuthorize("hasRole('TENANT_OWNER')")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final ApiKeyStatusCacheService cacheService;

    public ApiKeyController(ApiKeyService apiKeyService, ApiKeyStatusCacheService cacheService) {
        this.apiKeyService = apiKeyService;
        this.cacheService = cacheService;
    }

    /**
     * List all API keys for the current tenant.
     * Never returns plaintext keys.
     */
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listKeys() {
        TenantPrincipal principal = requireJwtPrincipal();
        assertTenantNotRevoked(principal.tenantId());
        return ResponseEntity.ok(apiKeyService.listKeys(principal.tenantId()));
    }

    /**
     * Create a new scoped API key.
     * Returns 201 with the plaintext key revealed once.
     */
    @PostMapping
    public ResponseEntity<ApiKeyCreatedResponse> createKey(@Valid @RequestBody CreateApiKeyRequest request) {
        TenantPrincipal principal = requireJwtPrincipal();
        assertTenantWritable(principal.tenantId());
        ApiKeyCreatedResponse response = apiKeyService.createKey(principal.tenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Revoke an API key by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeKey(@PathVariable("id") UUID id) {
        TenantPrincipal principal = requireJwtPrincipal();
        assertTenantWritable(principal.tenantId());
        apiKeyService.revokeKey(principal.tenantId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rotate an API key: creates a new key with inherited/overridden scopes and sets the old key's expiry to the grace period.
     * Returns 201 with the new plaintext key revealed once.
     */
    @PostMapping("/{id}/rotate")
    public ResponseEntity<ApiKeyCreatedResponse> rotateKey(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) RotateApiKeyRequest request) {
        TenantPrincipal principal = requireJwtPrincipal();
        assertTenantWritable(principal.tenantId());
        RotateApiKeyRequest effectiveRequest = request != null ? request : new RotateApiKeyRequest(null, null, null, null);
        ApiKeyCreatedResponse response = apiKeyService.rotateKey(principal.tenantId(), id, effectiveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private TenantPrincipal requireJwtPrincipal() {
        TenantPrincipal principal = TenantContext.require();
        if (!"JWT".equals(principal.authType())) {
            throw new DispatchXException(ErrorCode.FORBIDDEN, "API key management requires JWT authentication");
        }
        return principal;
    }

    private void assertTenantWritable(UUID tenantId) {
        TenantStatus status = cacheService.getTenantStatus(tenantId)
                .orElseThrow(() -> new DispatchXException(ErrorCode.NOT_FOUND, "Tenant not found"));

        if (status == TenantStatus.SUSPENDED) {
            throw new DispatchXException(ErrorCode.TENANT_SUSPENDED, "Tenant is suspended (read-only mode)");
        }
        if (status == TenantStatus.REVOKED) {
            throw new DispatchXException(ErrorCode.TENANT_REVOKED, "Tenant is revoked");
        }
    }

    private void assertTenantNotRevoked(UUID tenantId) {
        TenantStatus status = cacheService.getTenantStatus(tenantId)
                .orElseThrow(() -> new DispatchXException(ErrorCode.NOT_FOUND, "Tenant not found"));

        if (status == TenantStatus.REVOKED) {
            throw new DispatchXException(ErrorCode.TENANT_REVOKED, "Tenant is revoked");
        }
    }
}
