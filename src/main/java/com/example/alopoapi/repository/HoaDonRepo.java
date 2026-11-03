package com.example.alopoapi.repository;

import com.example.alopoapi.entity.HoaDonEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HoaDonRepo extends MongoRepository<HoaDonEntity, String> {
}
