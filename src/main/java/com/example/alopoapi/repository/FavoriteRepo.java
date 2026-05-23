package com.example.alopoapi.repository;

import com.example.alopoapi.entity.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepo extends JpaRepository<FavoriteEntity, String> {
    List<FavoriteEntity> findAllByMaNguoiDung(String maNguoiDung);

    boolean existsByMaNguoiDungAndMaSan(String maNguoiDung, String maSan);
}
