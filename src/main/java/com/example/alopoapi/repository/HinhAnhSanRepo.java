package com.example.alopoapi.repository;

import com.example.alopoapi.entity.HinhAnhSanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HinhAnhSanRepo extends MongoRepository<HinhAnhSanEntity, String> {
    List<HinhAnhSanEntity> findByMaCumSan(String maCumSan);
}
