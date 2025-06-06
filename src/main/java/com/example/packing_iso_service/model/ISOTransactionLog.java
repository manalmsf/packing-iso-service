package com.example.packing_iso_service.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ISOTransactionLog {
    private String type;
    private String mti;
    private String payload;
    private String status;
    private LocalDateTime timestamp;
    private String username;
    private String origin;

    public ISOTransactionLog(String type, String mti, String payload, String status, LocalDateTime timestamp, String username, String origin) {
        this.type = type;
        this.mti = mti;
        this.payload = payload;
        this.status = status;
        this.timestamp = timestamp;
        this.username = username;
        this.origin = origin;
    }

    public ISOTransactionLog() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setFields(Map<String, String> fields) {
    }
}