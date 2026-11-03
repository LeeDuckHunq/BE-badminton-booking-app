package com.example.alopoapi.repository;

import com.example.alopoapi.entity.HinhAnhSanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HinhAnhSanRepo extends MongoRepository<HinhAnhSanEntity, String> {
}
