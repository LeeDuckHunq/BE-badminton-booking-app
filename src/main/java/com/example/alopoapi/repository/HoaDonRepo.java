package com.example.alopoapi.repository;

import com.example.alopoapi.entity.HoaDonEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HoaDonRepo extends MongoRepository<HoaDonEntity, String> {
    Optional<HoaDonEntity> findFirstByMaPhieuDat(String maPhieuDat);

    boolean existsByMaPhieuDat(String maPhieuDat);
}
