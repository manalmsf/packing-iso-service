package com.example.packing_iso_service.model;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import java.util.Map;
@AllArgsConstructor
@Data
@NoArgsConstructor
@Document(collection = "packed_messages")
public class PackedMessage {

    @Id
    private String id;

    private String mti;
    private Map<String, String> fields;
    private String asciiMessage;
    private String hexMessage;
    private String format; // "ascii" ou "hex"
    private LocalDateTime timestamp;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getAsciiMessage() {
        return asciiMessage;
    }

    public void setAsciiMessage(String asciiMessage) {
        this.asciiMessage = asciiMessage;
    }

    public String getHexMessage() {
        return hexMessage;
    }

    public void setHexMessage(String hexMessage) {
        this.hexMessage = hexMessage;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
