package com.example.packing_iso_service.dto;

import java.util.Map;

public class UnpackedMessageDTO {
    private String mti;
    private Map<String, String> fields;

    public UnpackedMessageDTO(String mti, Map<String, String> fields) {
        this.mti = mti;
        this.fields = fields;
    }

    public String getMti() {
        return mti;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}
