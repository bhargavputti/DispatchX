package com.dispatchx.delivery.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Tuning of the in-app delivery worker ({@code dispatchx.worker.inapp.*}, / ). The defaults match the email and
 * webhook workers so the failure policy and the health thresholds behave identically per channel; the
 * worker stays disabled unless {@code dispatchx.worker.inapp.enabled=true} (, extended by ).
 */
@ConfigurationProperties(prefix = "dispatchx.worker.inapp")
public record InAppWorkerProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("dispatchx.dispatch.inapp.v1") String topic,
        @DefaultValue("dispatchx-inapp-delivery-workers") String groupId,
        @DefaultValue("true") boolean autoStartup,
        @DefaultValue("3s") Duration retryInterval,
        @DefaultValue("3") int retryAttempts,
        @DefaultValue("30s") Duration pauseInitial,
        @DefaultValue("300s") Duration pauseMax,
        @DefaultValue("3") int escalateAfter,
        @DefaultValue("5s") Duration healthPollInterval,
        @DefaultValue("2") int healthSuccesses,
        @DefaultValue("8s") Duration dltSendTimeout,
        @DefaultValue("1000") long lagThreshold,
        @DefaultValue("60s") Duration assignmentGrace
) implements WorkerTuning {

    @Override
    public String name() {
        return "inapp";
    }

    public InAppWorkerProperties {
        if (retryAttempts < 0) {
            throw new IllegalArgumentException("dispatchx.worker.inapp.retry-attempts must not be negative");
        }
        if (escalateAfter < 1) {
            throw new IllegalArgumentException("dispatchx.worker.inapp.escalate-after must be at least 1");
        }
        if (healthSuccesses < 1) {
            throw new IllegalArgumentException("dispatchx.worker.inapp.health-successes must be at least 1");
        }
        if (pauseMax.compareTo(pauseInitial) < 0) {
            throw new IllegalArgumentException("dispatchx.worker.inapp.pause-max must be at least pause-initial");
        }
    }
}