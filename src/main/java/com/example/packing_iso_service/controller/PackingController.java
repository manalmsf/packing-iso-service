package com.example.packing_iso_service.controller;
import org.apache.commons.codec.binary.Hex;

import com.example.packing_iso_service.dto.PackingRequest;
import com.example.packing_iso_service.dto.UnpackedMessageDTO;
import com.example.packing_iso_service.model.PackedMessage;
import com.example.packing_iso_service.model.UnpackedMessage;
import com.example.packing_iso_service.repository.PackedMessageRepository;
import com.example.packing_iso_service.repository.UnpackedMessageRepository;
import com.example.packing_iso_service.service.LogStreamService;
import com.example.packing_iso_service.service.PackingService;
import com.example.packing_iso_service.dto.UnpackRequest;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packing")
public class PackingController {

    @Autowired
    private GenericPackager isoPackager;

    private final PackingService packingService;
    @Autowired
    private PackedMessageRepository packedMessageRepository;
    @Autowired
    private UnpackedMessageRepository unpackedMessageRepository;

    @Autowired
    public PackingController(PackingService packingService) {
        this.packingService = packingService;
    }

    @PostMapping("/pack")
    public ResponseEntity<?> packMessage(@RequestBody PackingRequest request) {
        try {
            Map<String, Object> result = packingService.pack(request);


            return ResponseEntity.ok().body(Map.of(
                    "result", result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur côté serveur",
                    "details", e.getMessage()
            ));
        }
    }


    @GetMapping("/history")
    public List<PackedMessage> getHistory(
            @RequestParam(required = false) String mti,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (mti != null && date != null) {
            return packedMessageRepository.findByMtiAndDate(mti, date);
        } else if (mti != null) {
            return packedMessageRepository.findByMti(mti);
        } else if (date != null) {
            return packedMessageRepository.findByDate(date);
        }
        return packedMessageRepository.findAll();
    }
    @PostMapping("/unpack")
    public ResponseEntity<?> unpack(@RequestBody UnpackRequest request) {
        try {
            String format = request.getFormat();
            String message = request.getMessage();

            // Convertir en bytes selon le format
            byte[] messageBytes;
            if ("hex".equalsIgnoreCase(format)) {
                messageBytes = Hex.decodeHex(message.toCharArray());
            } else {
                messageBytes = message.getBytes(StandardCharsets.UTF_8);
            }

            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(isoPackager); // ton GenericPackager déjà défini
            isoMsg.unpack(messageBytes);

            String mti = isoMsg.getMTI();
            Map<String, String> fields = new HashMap<>();

            for (int i = 2; i <= 128; i++) {
                if (isoMsg.hasField(i)) {
                    fields.put(String.valueOf(i), isoMsg.getString(i));
                }
            }

            // Réponse propre
            UnpackedMessageDTO unpacked = new UnpackedMessageDTO(mti, fields);
            return ResponseEntity.ok(unpacked);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors du dépacking");
        }
    }


    @GetMapping("/unpack/history")
    public List<UnpackedMessage> getUnpackedHistory() {
        return unpackedMessageRepository.findAll();
    }
    @DeleteMapping("/history")
        public ResponseEntity<String> deleteHistory() {
            packedMessageRepository.deleteAll();
            return ResponseEntity.ok("Historique supprimé.");
        }   }
