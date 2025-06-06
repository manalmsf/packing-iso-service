package com.example.packing_iso_service.controller;

import com.example.packing_iso_service.dto.PackingRequest;
import com.example.packing_iso_service.model.PackedMessage;
import com.example.packing_iso_service.model.UnpackedMessage;
import com.example.packing_iso_service.repository.PackedMessageRepository;
import com.example.packing_iso_service.repository.UnpackedMessageRepository;
import com.example.packing_iso_service.service.LogStreamService;
import com.example.packing_iso_service.service.PackingService;
import com.example.packing_iso_service.dto.UnpackRequest;
import org.jpos.iso.ISOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packing")
public class PackingController {


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
    public String unpackMessage(@RequestBody UnpackRequest request) throws Exception {
        {
            return packingService.unpack(request);

        }}


    @GetMapping("/unpack/history")
    public List<UnpackedMessage> getUnpackedHistory() {
        return unpackedMessageRepository.findAll();
    }
    @DeleteMapping("/history")
        public ResponseEntity<String> deleteHistory() {
            packedMessageRepository.deleteAll();
            return ResponseEntity.ok("Historique supprimé.");
        }   }
