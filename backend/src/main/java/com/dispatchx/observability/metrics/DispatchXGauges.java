package com.dispatchx.observability.metrics;

import com.dispatchx.common.model.NotificationStatus;
import com.dispatchx.notification.service.NotificationQueryService;
import com.dispatchx.outbox.service.OutboxQueryService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;

/**
 * Database- and breaker-backed gauges for / . Each gauge pulls its value on scrape through a feature
 * query service (never a repository directly), so there is no background polling and no core-path change.
 */
@Component
public class DispatchXGauges implements MeterBinder {

    private final NotificationQueryService notifications;
    private final OutboxQueryService outbox;
    private final Map<String, CircuitBreaker> circuitBreakers;
    private final Clock clock;

    public DispatchXGauges(NotificationQueryService notifications,
                        OutboxQueryService outbox,
                        Map<String, CircuitBreaker> circuitBreakers,
                        Clock clock) {
        this.notifications = notifications;
        this.outbox = outbox;
        this.circuitBreakers = circuitBreakers;
        this.clock = clock;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("dispatchx.queue.depth", notifications,
                        n -> n.countByStatusGlobal(NotificationStatus.QUEUED))
                .tag("status", "QUEUED").register(registry);
        Gauge.builder("dispatchx.queue.depth", notifications,
                        n -> n.countByStatusGlobal(NotificationStatus.RETRYING))
                .tag("status", "RETRYING").register(registry);

        Gauge.builder("dispatchx.outbox.pending", outbox, OutboxQueryService::pendingCount)
                .register(registry);
        Gauge.builder("dispatchx.outbox.oldest.age.seconds", this, DispatchXGauges::oldestOutboxAgeSeconds)
                .register(registry);

        Gauge.builder("dispatchx.dlq.open", notifications, NotificationQueryService::dlqCountGlobal)
                .register(registry);

        circuitBreakers.forEach((beanName, breaker) ->
                Gauge.builder("dispatchx.resilience.circuitbreaker.state", breaker, b -> b.getState().getOrder())
                        .tag("name", breaker.getName() != null ? breaker.getName() : beanName)
                        .register(registry));
    }

    private double oldestOutboxAgeSeconds() {
        return outbox.oldestPendingCreatedAt()
                .map(oldest -> Math.max(0.0, (clock.millis() - oldest.toEpochMilli()) / 1000.0))
                .orElse(0.0);
    }
}
