package com.dispatchx.dlq.consumer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Refusal cause 2: the input parses but names another environment and another group (/ ; ).
 *
 * <p>This is the input-prepared-for-the-wrong-place case. It reaches further into the composition than an
 * unreadable file does - the file is read, parsed and validated, an admin client is created - and it must still
 * leave both listeners stopped with nothing committed.
 */
@TestPropertySource(properties = {
        "dispatchx.dlq.consumer.enabled=true",
        "dispatchx.dlq.dlt-consumer.enabled=true",
        "dispatchx.dlq.consumer.group-id=n17-refusal-dlq",
        "dispatchx.dlq.dlt-consumer.group-id=n17-refusal-dlt",
        "dispatchx.worker.email.enabled=false",
        "dispatchx.worker.webhook.enabled=false",
        "dispatchx.worker.inapp.enabled=false",
        "dispatchx.recovery.enabled=false",
        "dispatchx.reconciler.enabled=false",
        "dispatchx.outbox.relay-enabled=false"
})
class DlqStartupMismatchedInputContextTest extends AbstractDlqStartupRefusalTest {

    private static Path input;

    static {
        try {
            input = Files.createTempFile("n17-mismatched-offsets-", ".json");
            Files.writeString(input, "{\"environment\":\"staging\",\"groupId\":\"some-other-group\","
                    + "\"offsets\":[{\"topic\":\"dispatchx.dlq.v1\",\"partition\":0,\"nextOffset\":0}]}");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void mismatchedInput(DynamicPropertyRegistry registry) {
        registry.add("dispatchx.dlq.offset-init-file", () -> input.toString());
    }

    @Override
    String expectedReasonFragment() {
        // The group check fires first for a listener whose group the input does not name.
        return "which is not the group of listener";
    }
}
