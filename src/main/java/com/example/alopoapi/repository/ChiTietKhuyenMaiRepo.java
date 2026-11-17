package com.example.alopoapi.repository;

import com.example.alopoapi.entity.voucher.ChiTietKhuyenMaiEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChiTietKhuyenMaiRepo extends MongoRepository<ChiTietKhuyenMaiEntity, String> {
    List<ChiTietKhuyenMaiEntity> findByMaNguoiDung(String maNguoiDung);

    boolean existsByMaNguoiDungAndMaKM(String maNguoiDung, String maKM);
}
