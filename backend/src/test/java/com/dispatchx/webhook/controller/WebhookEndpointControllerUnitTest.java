package com.dispatchx.webhook.controller;

import com.dispatchx.apikey.cache.ApiKeyStatusCacheService;
import com.dispatchx.common.context.TenantContext;
import com.dispatchx.common.context.TenantPrincipal;
import com.dispatchx.common.error.ErrorCode;
import com.dispatchx.common.error.DispatchXException;
import com.dispatchx.common.model.TenantStatus;
import com.dispatchx.webhook.service.WebhookEndpointService;
import com.dispatchx.webhook.service.WebhookPingSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookEndpointControllerUnitTest {

    @Mock private WebhookEndpointService endpointService;
    @Mock private WebhookPingSender pingSender;
    @Mock private ApiKeyStatusCacheService tenantStatusCache;

    private WebhookEndpointController controller;
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new WebhookEndpointController(endpointService, pingSender, tenantStatusCache);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("delete succeeds for writable tenant with JWT auth and returns 204 No Content")
    void deleteSucceeds() {
        TenantContext.set(TenantPrincipal.forUser(tenantId, UUID.randomUUID(), Set.of("ROLE_TENANT_OWNER")));
        when(tenantStatusCache.getTenantStatus(tenantId)).thenReturn(Optional.of(TenantStatus.ACTIVE));

        ResponseEntity<Void> response = controller.delete();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(endpointService).delete(tenantId);
    }

    @Test
    @DisplayName("delete fails with FORBIDDEN if authentication is not JWT")
    void deleteRequiresJwt() {
        TenantContext.set(TenantPrincipal.forApiKey(tenantId, UUID.randomUUID(), Set.of("ROLE_TENANT_OWNER")));

        assertThatThrownBy(() -> controller.delete())
                .isInstanceOf(DispatchXException.class)
                .satisfies(ex -> assertThat(((DispatchXException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verifyNoInteractions(endpointService);
    }

    @Test
    @DisplayName("delete fails with TENANT_SUSPENDED if tenant is suspended")
    void deleteFailsWhenSuspended() {
        TenantContext.set(TenantPrincipal.forUser(tenantId, UUID.randomUUID(), Set.of("ROLE_TENANT_OWNER")));
        when(tenantStatusCache.getTenantStatus(tenantId)).thenReturn(Optional.of(TenantStatus.SUSPENDED));

        assertThatThrownBy(() -> controller.delete())
                .isInstanceOf(DispatchXException.class)
                .satisfies(ex -> assertThat(((DispatchXException) ex).getErrorCode()).isEqualTo(ErrorCode.TENANT_SUSPENDED));

        verifyNoInteractions(endpointService);
    }

    @Test
    @DisplayName("delete propagates NOT_FOUND when endpointService throws NOT_FOUND")
    void deletePropagatesNotFound() {
        TenantContext.set(TenantPrincipal.forUser(tenantId, UUID.randomUUID(), Set.of("ROLE_TENANT_OWNER")));
        when(tenantStatusCache.getTenantStatus(tenantId)).thenReturn(Optional.of(TenantStatus.ACTIVE));
        doThrow(new DispatchXException(ErrorCode.NOT_FOUND, "No webhook endpoint is configured"))
                .when(endpointService).delete(tenantId);

        assertThatThrownBy(() -> controller.delete())
                .isInstanceOf(DispatchXException.class)
                .satisfies(ex -> assertThat(((DispatchXException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }
}
