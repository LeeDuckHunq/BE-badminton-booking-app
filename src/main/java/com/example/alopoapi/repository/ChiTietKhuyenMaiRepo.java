package com.example.alopoapi.repository;

import com.example.alopoapi.entity.voucher.ChiTietKhuyenMaiEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChiTietKhuyenMaiRepo extends MongoRepository<ChiTietKhuyenMaiEntity, String> {
}
