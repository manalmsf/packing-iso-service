package com.example.packing_iso_service.service;

import com.example.packing_iso_service.dto.ISOMessageRequest;
import com.example.packing_iso_service.dto.PackingRequest;
import com.example.packing_iso_service.dto.UnpackRequest;
import com.example.packing_iso_service.model.ISOTransactionLog;
import com.example.packing_iso_service.model.PackedMessage;
import com.example.packing_iso_service.model.UnpackedMessage;
import com.example.packing_iso_service.repository.PackedMessageRepository;
import com.example.packing_iso_service.repository.UnpackedMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PackingService {

    @Autowired
    private LogStreamService logStreamService;

    @Autowired
    private PackedMessageRepository packedMessageRepository;

    @Autowired
    private DualSendingService dualSendingService;

    @Autowired
    private UnpackedMessageRepository unpackedMessageRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    Map<String, Object> validationResult = null;
    private final GenericPackager packager;

    public PackingService() throws IOException, ISOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("iso8583-jpos-packager.xml");
        if (is == null) throw new RuntimeException("Packager file not found!");
        this.packager = new GenericPackager(is);
    }

    public void logTransaction(String type, String mti, String payload, String username) {
        try {
            ISOTransactionLog log = new ISOTransactionLog(
                    type, mti, payload, "SUCCESS", LocalDateTime.now(), username, "packing-service"
            );
            String json = objectMapper.writeValueAsString(log);
            rabbitTemplate.convertAndSend("iso.transaction.log.queue", json);
            System.out.println("✅ Log envoyé à RabbitMQ : " + json);
        } catch (Exception e) {
            System.err.println("❌ Erreur d’envoi du log à RabbitMQ : " + e.getMessage());
        }
    }

    public Map<String, Object> pack(PackingRequest request) throws IOException {
        String asciiString = "";
        String hexString = "";
        String mti = request.getMti();
        Map<String, String> fields = request.getFields();
        Map<String, Object> resultMap = new LinkedHashMap<>();
        Map<String, Object> validationResult = null;

        PackedMessage msg = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";

        try {
            logStreamService.sendLog("INFO", "📦 Début du traitement du message ISO");

            if (mti == null || !mti.matches("\\d{4}"))
                throw new IllegalArgumentException("MTI invalide : il doit être un entier de 4 chiffres.");

            if (!fields.containsKey("11") || fields.get("11").isBlank())
                fields.put("11", String.format("%06d", (int) (Math.random() * 1_000_000)));

            if (!fields.containsKey("37") || fields.get("37").isBlank())
                fields.put("37", UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase());

            if (!fields.containsKey("41") || fields.get("41").isBlank())
                fields.put("41", "ATM" + (int) (Math.random() * 100));

            if (!fields.containsKey("2") || !fields.get("2").matches("\\d{13,19}"))
                throw new IllegalArgumentException("Champ 2 (PAN) invalide.");
            if (!fields.containsKey("3") || !fields.get("3").matches("\\d{6}"))
                throw new IllegalArgumentException("Champ 3 (Processing Code) invalide.");
            if (!fields.containsKey("4") || !fields.get("4").matches("\\d{12}"))
                throw new IllegalArgumentException("Champ 4 (Amount) invalide.");
            if (!fields.containsKey("7") || !fields.get("7").matches("\\d{10}"))
                throw new IllegalArgumentException("Champ 7 (Transmission Date & Time) invalide.");
            if (!fields.containsKey("11") || !fields.get("11").matches("\\d{6}"))
                throw new IllegalArgumentException("Champ 11 (STAN) invalide.");
            if (!fields.containsKey("41") || fields.get("41").length() < 4)
                throw new IllegalArgumentException("Champ 41 (Terminal ID) invalide.");
            if (!fields.containsKey("49") || !fields.get("49").matches("\\d{3}"))
                throw new IllegalArgumentException("Champ 49 (Currency Code) invalide.");

            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setMTI(mti);

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                int fieldId = Integer.parseInt(entry.getKey());
                isoMsg.set(fieldId, entry.getValue());
            }

            byte[] packedBytes = isoMsg.pack();
            asciiString = new String(packedBytes, StandardCharsets.US_ASCII);
            hexString = ISOUtil.hexString(packedBytes);

            msg = new PackedMessage();
            msg.setMti(mti);
            msg.setFields(fields);
            msg.setAsciiMessage(asciiString);
            msg.setHexMessage(hexString);
            msg.setFormat("ascii+hex");
            msg.setTimestamp(LocalDateTime.now());
            packedMessageRepository.save(msg);

            logTransaction("PACK", mti, asciiString, username);

        } catch (Exception e) {
            logStreamService.sendLog("ERROR", "❌ Erreur packing : " + e.getMessage());
            e.printStackTrace();
        }

        // Envoi pour validation
        ISOMessageRequest validationRequest = new ISOMessageRequest(mti, fields, false);
        validationRequest.setUsername(username);
        String validationJson = objectMapper.writeValueAsString(validationRequest);
        rabbitTemplate.convertAndSend("iso.validation.request.queue", validationJson);

        try {
            String result = (String) rabbitTemplate.receiveAndConvert("iso.validation.result.queue", 5000);
            if (result != null) {
                validationResult = objectMapper.readValue(result, Map.class);

                // ✅ Masquage du PAN dans validationResult
                if (validationResult.containsKey("fields")) {
                    Object fieldsObj = validationResult.get("fields");
                    if (fieldsObj instanceof Map<?, ?> rawMap) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fieldsMap = (Map<String, Object>) rawMap;

                        Object panValue = fieldsMap.get("2");
                        if (panValue instanceof String pan && pan.length() >= 10) {
                            fieldsMap.put("2", maskCardNumber(pan));
                        }
                    }
                }


                if ("INVALID".equals(validationResult.get("validationStatus"))) {
                    Map<String, Object> errorMap = new LinkedHashMap<>();
                    errorMap.put("message", "Validation failed");
                    errorMap.put("reason", validationResult.get("responseCode"));
                    errorMap.put("errors", validationResult.get("errors"));
                    errorMap.put("explanation", validationResult.get("explanation"));
                    errorMap.put("mti", validationResult.get("mti"));
                    errorMap.put("asciiMessage", asciiString);
                    errorMap.put("hexMessage", hexString);
                    return errorMap;
                }
            }
        } catch (Exception e) {
            logStreamService.sendLog("ERROR", "❌ Erreur réception validation : " + e.getMessage());
        }

        resultMap.put("status", "VALID");
        resultMap.put("asciiMessage", asciiString);
        resultMap.put("hexMessage", hexString);
        resultMap.put("id", msg != null ? msg.getId() : null);

        // ✅ Masquage du PAN dans resultMap['fields']
        Map<String, Object> castedFields = new LinkedHashMap<>();
        fields.forEach(castedFields::put);
        if (castedFields.containsKey("2")) {
            castedFields.put("2", maskCardNumber(castedFields.get("2").toString()));
        }
        resultMap.put("fields", castedFields);

        // ✅ Métadonnées
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (validationResult != null && validationResult.get("metadata") instanceof Map) {
            Map<String, Object> metadataFromValidation = (Map<String, Object>) validationResult.get("metadata");
            if (metadataFromValidation.containsKey("validationId")) {
                metadata.put("validationId", metadataFromValidation.get("validationId").toString());
            }
        }
        resultMap.put("metadata", metadata);

        // ✅ Masquage du PAN dans les envois externes (REST/WebSocket)
        Map<String, String> safeFields = new LinkedHashMap<>(fields);
        if (safeFields.containsKey("2")) {
            safeFields.put("2", maskCardNumber(safeFields.get("2")));
        }

        try {
            dualSendingService.sendViaRest(mti, asciiString, hexString, safeFields);
            dualSendingService.sendViaWebSocket(mti, asciiString, hexString, safeFields);
        } catch (Exception e) {
            logStreamService.sendLog("ERROR", "❌ Dual sending échoué : " + e.getMessage());
        }

        return resultMap;
    }


    public String unpack(UnpackRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";

            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);

            byte[] raw;
            if ("hex".equalsIgnoreCase(request.getFormat())) {
                String hex = request.getMessage();
                if (hex.length() % 2 != 0) {
                    hex = "0" + hex;
                }
                raw = ISOUtil.hex2byte(hex);
            } else if ("ascii".equalsIgnoreCase(request.getFormat())) {
                raw = request.getMessage().getBytes(StandardCharsets.US_ASCII);
            } else {
                return "{\"error\": \"Invalid format. Use 'hex' or 'ascii'.\"}";
            }

            isoMsg.unpack(raw);

            Map<String, String> unpacked = new LinkedHashMap<>();
            unpacked.put("MTI", isoMsg.getMTI());
            for (int i = 1; i <= 128; i++) {
                if (isoMsg.hasField(i)) {
                    String value = isoMsg.getString(i);
                    if (i == 2 && value.length() >= 10) {
                        value = maskCardNumber(value);
                    }
                    unpacked.put("Field " + i, value);
                }
            }


            UnpackedMessage msg = new UnpackedMessage();
            msg.setOriginalMessage(request.getMessage());
            msg.setFormat(request.getFormat());
            msg.setMti(isoMsg.getMTI());
            msg.setFields(unpacked);
            msg.setTimestamp(LocalDateTime.now());
            unpackedMessageRepository.save(msg);

            logTransaction("UNPACK", isoMsg.getMTI(), request.getMessage(), username);

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(unpacked);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    // ✅ Masquage du PAN
    private String maskCardNumber(String pan) {
        if (pan == null || pan.length() < 10) return "****";
        return pan.substring(0, 4) + "********" + pan.substring(pan.length() - 6);
    }
}
