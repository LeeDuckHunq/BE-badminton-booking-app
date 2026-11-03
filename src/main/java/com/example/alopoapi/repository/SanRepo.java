package com.example.alopoapi.repository;

import com.example.alopoapi.entity.SanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SanRepo extends MongoRepository<SanEntity, String> {
}
