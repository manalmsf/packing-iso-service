package com.example.packing_iso_service.service;

import com.example.packing_iso_service.util.ISOFieldTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.net.URI;


@Service
public class DualSendingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DualSendingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendViaRest(String mti, String ascii, String hex, Map<String, String> fields) {
        try {
            String url = "https://webhook.site/746a9636-ddb4-4aef-9095-3f18d5ebfb36";

            // Données à envoyer
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mti", mti);
            body.put("ascii", ascii);
            body.put("hex", hex);
            body.put("fields", ISOFieldTransformer.transformFields(fields));


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ REST Dual Sending OK : " + response.getBody());
            } else {
                System.err.println("❌ REST Dual Sending ERROR : " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Exception during REST Dual Sending: " + e.getMessage());
        }
    }

    public void sendViaWebSocket(String mti, String ascii, String hex, Map<String, String> fields) {
        try {
            String uri = "wss://echo.websocket.events"; // URL de test WebSocket

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("mti", mti);
            message.put("ascii", ascii);
            message.put("hex", hex);
            message.put("fields", ISOFieldTransformer.transformFields(fields));

            String payload = objectMapper.writeValueAsString(message);

            HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(uri), new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket webSocket) {
                            System.out.println("🔗 WebSocket connecté.");
                            webSocket.sendText(payload, true);
                            webSocket.request(1);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            System.out.println("📨 Réponse WebSocket : " + data);
                            return null;
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            System.err.println("❌ Erreur WebSocket : " + error.getMessage());
                        }
                    });

        } catch (Exception e) {
            System.err.println("❌ Exception WebSocket Dual Sending : " + e.getMessage());
        }
    }
}