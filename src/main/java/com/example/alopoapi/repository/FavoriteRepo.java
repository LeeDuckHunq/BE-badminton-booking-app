package com.example.alopoapi.repository;

import com.example.alopoapi.entity.FavoriteEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepo extends MongoRepository<FavoriteEntity, String> {
    List<FavoriteEntity> findAllByMaNguoiDung(String maNguoiDung);

    boolean existsByMaNguoiDungAndMaCumSan(String maNguoiDung, String maCumSan);
}
