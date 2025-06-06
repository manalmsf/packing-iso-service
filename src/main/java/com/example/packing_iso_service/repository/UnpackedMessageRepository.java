package com.example.packing_iso_service.repository;

import com.example.packing_iso_service.model.UnpackedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UnpackedMessageRepository extends MongoRepository<UnpackedMessage, String> {
}