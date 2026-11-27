package com.dispatchx.demo;

import com.dispatchx.common.error.ErrorCode;
import com.dispatchx.common.error.DispatchXException;
import com.dispatchx.common.model.Channel;
import com.dispatchx.common.model.NotificationStatus;
import com.dispatchx.demo.config.DemoProperties;
import com.dispatchx.demo.controller.DemoStreamController;
import com.dispatchx.demo.dto.DemoInboxItemResponse;
import com.dispatchx.demo.service.DemoInboxService;
import com.dispatchx.demo.service.DemoStreamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoStreamControllerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-28T10:00:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("stream returns 404 when demo surface is disabled")
    void streamRefusedWhenDisabled() {
        DemoProperties props = new DemoProperties("", false, List.of());
        DemoStreamService streamService = new DemoStreamService(props, clock);
        DemoStreamController controller = new DemoStreamController(props, streamService);

        assertThatThrownBy(controller::stream)
                .isInstanceOf(DispatchXException.class)
                .satisfies(e -> {
                    DispatchXException ne = (DispatchXException) e;
                    assertThat(ne.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("stream returns 404 when tenant slugs are empty despite enabled flag")
    void streamRefusedWhenNoSlugs() {
        DemoProperties props = new DemoProperties("", true, List.of());
        DemoStreamService streamService = new DemoStreamService(props, clock);
        DemoStreamController controller = new DemoStreamController(props, streamService);

        assertThatThrownBy(controller::stream)
                .isInstanceOf(DispatchXException.class)
                .satisfies(e -> {
                    DispatchXException ne = (DispatchXException) e;
                    assertThat(ne.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("stream returns active SseEmitter when enabled with demo tenant slugs")
    void streamSubscribesWhenEnabled() {
        DemoProperties props = new DemoProperties("", true, List.of("acme", "globex"));
        DemoStreamService streamService = new DemoStreamService(props, clock);
        DemoStreamController controller = new DemoStreamController(props, streamService);

        SseEmitter emitter = controller.stream();
        assertThat(emitter).isNotNull();
        assertThat(streamService.activeSubscriberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("recipient masking masks email and url recipients securely")
    void maskingEnforced() {
        assertThat(DemoInboxService.maskRecipient("john.doe@example.com")).isEqualTo("j***@example.com");
        assertThat(DemoInboxService.maskRecipient("a@domain.com")).isEqualTo("a***@domain.com");
        assertThat(DemoInboxService.maskRecipient("https://webhook.site/test")).startsWith("h***");
        assertThat(DemoInboxService.maskRecipient("")).isEqualTo("***");
        assertThat(DemoInboxService.maskRecipient(null)).isEqualTo("***");
    }

    @Test
    @DisplayName("subject sanitization strips HTML tags and limits length")
    void sanitizationEnforced() {
        assertThat(DemoInboxService.sanitizeSubject("<script>alert(1)</script>Hello!"))
                .doesNotContain("<")
                .doesNotContain(">");
        assertThat(DemoInboxService.sanitizeSubject(null)).isNull();

        String longSubject = "A".repeat(100);
        assertThat(DemoInboxService.sanitizeSubject(longSubject).length()).isLessThanOrEqualTo(64);
    }

    @Test
    @DisplayName("simulationPulse generates compliant events when subscribers exist")
    void simulationPulseEmitsWhenActive() {
        DemoProperties props = new DemoProperties("", true, List.of("acme", "globex"));
        DemoStreamService streamService = new DemoStreamService(props, clock);

        // Before subscriber: pulse is a no-op
        streamService.simulationPulse();
        assertThat(streamService.activeSubscriberCount()).isEqualTo(0);

        // Subscribe an emitter
        SseEmitter emitter = streamService.subscribe();
        assertThat(streamService.activeSubscriberCount()).isEqualTo(1);

        // Trigger pulse
        streamService.simulationPulse();
        // Emitter remains active and healthy
        assertThat(streamService.activeSubscriberCount()).isEqualTo(1);

        // Trigger heartbeat
        streamService.sendHeartbeat();
        assertThat(streamService.activeSubscriberCount()).isEqualTo(1);
    }
}
