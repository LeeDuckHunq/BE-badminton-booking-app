package com.example.alopoapi.repository;

import com.example.alopoapi.entity.voucher.KhuyenMaiEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KhuyenMaiRepo extends MongoRepository<KhuyenMaiEntity, String> {
}
