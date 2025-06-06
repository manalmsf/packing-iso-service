package com.example.packing_iso_service.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class PackingRequest {
    private String mti;
    private Map<String,String> fields;
    private String format; // "ascii" or "hex"
    private boolean dualSending;

    // getters & setters
    public String getMti() { return mti; }
    public void setMti(String mti) { this.mti = mti; }

    public Map<String,String> getFields() { return fields; }
    public void setFields(Map<String,String> fields) { this.fields = fields; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public boolean isDualSending() {
        return dualSending;
    }

    public void setDualSending(boolean dualSending) {
        this.dualSending = dualSending;
    }

}