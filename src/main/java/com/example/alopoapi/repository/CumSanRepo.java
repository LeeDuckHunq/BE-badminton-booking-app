package com.example.alopoapi.repository;

import com.example.alopoapi.entity.CumSanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CumSanRepo extends MongoRepository<CumSanEntity, String> {
    List<CumSanEntity> findByOwnerUsername(String ownerUsername);
}
