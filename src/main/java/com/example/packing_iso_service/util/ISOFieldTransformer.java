package com.example.packing_iso_service.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ISOFieldTransformer {
    public static Map<String, String> transformFields(Map<String, String> originalFields) {
        Map<String, String> transformed = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : originalFields.entrySet()) {
            String fieldKey = entry.getKey();
            String value = entry.getValue();
            int fieldId;

            try {
                fieldId = fieldKey.startsWith("field_") ? Integer.parseInt(fieldKey.substring(6)) : Integer.parseInt(fieldKey);
            } catch (NumberFormatException e) {
                transformed.put(fieldKey, value);
                continue;
            }

            String label = ISOFieldNames.FIELD_NAMES.getOrDefault(fieldId, "Field " + fieldId);
            if (fieldId == 2 && value.length() >= 8)
                value = value.substring(0, 4) + "****" + value.substring(value.length() - 4);
            transformed.put(label, value);
        }

        return transformed;
    }
}
