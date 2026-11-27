package com.dispatchx.demo.controller;

import com.dispatchx.common.error.ErrorCode;
import com.dispatchx.common.error.DispatchXException;
import com.dispatchx.demo.config.DemoProperties;
import com.dispatchx.demo.service.DemoStreamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Public live demo SSE stream (CONTRACTS §Demo, / ).
 * Streams safe public demo inbox events for configured demo tenants.
 * Gated by dispatchx.demo.public + dispatchx.demo.tenant-slugs.
 */
@RestController
public class DemoStreamController {

    private final DemoProperties demoProperties;
    private final DemoStreamService demoStreamService;

    public DemoStreamController(DemoProperties demoProperties, DemoStreamService demoStreamService) {
        this.demoProperties = demoProperties;
        this.demoStreamService = demoStreamService;
    }

    @GetMapping(value = {"/demo/stream", "/api/v1/demo/stream"})
    public SseEmitter stream() {
        if (!demoProperties.inboxEnabled()) {
            throw new DispatchXException(ErrorCode.NOT_FOUND, "Demo stream is not available");
        }
        return demoStreamService.subscribe();
    }
}
