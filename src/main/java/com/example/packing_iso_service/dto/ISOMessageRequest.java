package com.example.packing_iso_service.dto;

import java.util.Map;

public class ISOMessageRequest {
    private String mti;
    private Map<String, String> fields;
    private boolean pedagogicalMode;

    public ISOMessageRequest() {}

    public ISOMessageRequest(String mti, Map<String, String> fields, boolean pedagogicalMode) {
        this.mti = mti;
        this.fields = fields;
        this.pedagogicalMode = pedagogicalMode;
    }

    public String getMti() { return mti; }
    public void setMti(String mti) { this.mti = mti; }

    public Map<String, String> getFields() { return fields; }
    public void setFields(Map<String, String> fields) { this.fields = fields; }

    public boolean isPedagogicalMode() { return pedagogicalMode; }
    public void setPedagogicalMode(boolean pedagogicalMode) { this.pedagogicalMode = pedagogicalMode; }
}