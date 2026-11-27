package com.dispatchx.delivery.worker.control;

import com.dispatchx.delivery.worker.config.WebhookWorkerProperties;

import java.time.Clock;

/**
 * The {@code webhookWorker} health component (, same thresholds as the email worker). Outside the readiness
 * group: a paused or stopped webhook worker must never take the ingress API, or the email worker, out of rotation.
 */
public class WebhookWorkerHealthIndicator extends WorkerHealthIndicator {

    public WebhookWorkerHealthIndicator(
            WorkerContainerController controller, WebhookWorkerProperties props, Clock clock) {
        super(controller, props, clock);
    }
}
