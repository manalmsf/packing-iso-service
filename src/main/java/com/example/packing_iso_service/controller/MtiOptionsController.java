package com.example.packing_iso_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packing/mti")
public class MtiOptionsController {

    @GetMapping("/version")
    public List<Map<String, String>> getVersionOptions() {
        return List.of(
                Map.of("code", "0", "label", "ISO 8583:1987"),
                Map.of("code", "1", "label", "ISO 8583:1993"),
                Map.of("code", "2", "label", "ISO 8583:2003")
        );
    }

    @GetMapping("/class")
    public List<Map<String, String>> getClassOptions() {
        return List.of(
                Map.of("code", "1", "label", "Authorization "),
                Map.of("code", "2", "label", "Financial "),
                Map.of("code", "3", "label", "File actions "),
                Map.of("code", "4", "label", "Reversal / Charge-back "),
                Map.of("code", "5", "label", "Reconciliation "),
                Map.of("code", "6", "label", "Administrative "),
                Map.of("code", "7", "label", "Fee Collection "),
                Map.of("code", "8", "label", "Network Management ")
        );
    }

    @GetMapping("/function")
    public List<Map<String, String>> getFunctionOptions() {
        return List.of(
                Map.of("code", "0", "label", "Request "),
                Map.of("code", "1", "label", "Request Response "),
                Map.of("code", "2", "label", "Advice "),
                Map.of("code", "3", "label", "Advice Response "),
                Map.of("code", "4", "label", "Notification "),
                Map.of("code", "5", "label", "Notification Ack "),
                Map.of("code", "6", "label", "Instruction "),
                Map.of("code", "7", "label", "Instruction Ack ")
        );
    }

    @GetMapping("/originator")
    public List<Map<String, String>> getOriginatorOptions() {
        return List.of(
                Map.of("code", "0", "label", "Acquirer "),
                Map.of("code", "1", "label", "Acquirer "),
                Map.of("code", "2", "label", "Issuer "),
                Map.of("code", "3", "label", "Issuer Repeat "),
                Map.of("code", "4", "label", "Other ")
        );
    }
}
