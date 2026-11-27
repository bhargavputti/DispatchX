package com.dispatchx.dlq.consumer;

import org.springframework.test.context.TestPropertySource;

/**
 * Refusal cause 1: the configured initialization input cannot be read (/ ; ).
 *
 * <p>A configured-but-unreadable input is the operational case where a file was moved, mistyped or not mounted. It
 * must not degrade into "no input configured", which would start the listeners.
 */
@TestPropertySource(properties = {
        "dispatchx.dlq.consumer.enabled=true",
        "dispatchx.dlq.dlt-consumer.enabled=true",
        "dispatchx.dlq.consumer.group-id=n17-refusal-dlq",
        "dispatchx.dlq.dlt-consumer.group-id=n17-refusal-dlt",
        "dispatchx.dlq.offset-init-file=${java.io.tmpdir}/n17-offset-input-that-does-not-exist.json",
        "dispatchx.worker.email.enabled=false",
        "dispatchx.worker.webhook.enabled=false",
        "dispatchx.worker.inapp.enabled=false",
        "dispatchx.recovery.enabled=false",
        "dispatchx.reconciler.enabled=false",
        "dispatchx.outbox.relay-enabled=false"
})
class DlqStartupRefusalContextTest extends AbstractDlqStartupRefusalTest {

    @Override
    String expectedReasonFragment() {
        return "is not readable";
    }
}
