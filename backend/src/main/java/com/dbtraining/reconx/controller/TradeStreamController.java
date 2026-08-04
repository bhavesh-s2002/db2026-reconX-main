package com.dbtraining.reconx.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TICKET-ADV120 — Real-time Server-Sent Events (SSE) trade stream endpoint
 */
@RestController
@RequestMapping("/v1/trades")
@Tag(name = "trades-stream", description = "Real-time SSE Trade Stream")
public class TradeStreamController {

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to live trade stream via SSE")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L); // Infinite timeout
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("{\"status\":\"connected\"}"));
            } catch (Exception e) {
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, 15, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);
        emitter.onError(e -> executor.shutdown());

        return emitter;
    }
}
