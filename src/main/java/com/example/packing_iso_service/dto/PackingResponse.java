// 📁 com.example.packing_iso_service.dto.PackingResponse.java
package com.example.packing_iso_service.dto;

public class PackingResponse {
    private String asciiMessage;
    private String hexMessage;
    private String status;
    private String id; // ✅ pour le PDF

    public PackingResponse() {}

    public PackingResponse(String asciiMessage, String hexMessage, String status, String id) {
        this.asciiMessage = asciiMessage;
        this.hexMessage = hexMessage;
        this.status = status;
        this.id = id;
    }

    public String getAsciiMessage() { return asciiMessage; }
    public void setAsciiMessage(String asciiMessage) { this.asciiMessage = asciiMessage; }

    public String getHexMessage() { return hexMessage; }
    public void setHexMessage(String hexMessage) { this.hexMessage = hexMessage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
