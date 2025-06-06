package com.example.packing_iso_service.repository;


import com.example.packing_iso_service.model.PackedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PackedMessageRepository extends MongoRepository<PackedMessage, String> {
    List<PackedMessage> findByMti(String mti);

    @Query("{ 'timestamp': { $gte: ?0, $lt: ?1 } }")
    List<PackedMessage> findByDate(LocalDate dateStart);

    @Query("{ 'mti': ?0, 'timestamp': { $gte: ?1, $lt: ?2 } }")
    List<PackedMessage> findByMtiAndDate(String mti, LocalDate dateStart);

}
