package com.example.alopoapi.repository;

import com.example.alopoapi.entity.CumSanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CumSanRepo extends MongoRepository<CumSanEntity, String> {
}
