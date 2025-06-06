package com.example.packing_iso_service.controller;

import com.example.packing_iso_service.service.LogStreamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogStreamService logStreamService;

    public LogController(LogStreamService logStreamService) {
        this.logStreamService = logStreamService;
    }

    @GetMapping("/stream")
    public SseEmitter streamLogs() {
        return logStreamService.subscribe();
    }
}
