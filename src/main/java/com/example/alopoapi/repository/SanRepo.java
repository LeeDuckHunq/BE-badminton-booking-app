package com.example.alopoapi.repository;

import com.example.alopoapi.entity.SanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SanRepo extends MongoRepository<SanEntity, String> {
    List<SanEntity> findByMaCumSan(String maCumSan);
    List<SanEntity> findByMaCumSanIn(List<String> maCumSan);
}
