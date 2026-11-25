package com.example.alopoapi.repository;

import com.example.alopoapi.entity.nhom.NhomEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhomRepo extends MongoRepository<NhomEntity, String> {

    Optional<NhomEntity> findByMaMoi(String maMoi);

    boolean existsByMaMoi(String maMoi);

    List<NhomEntity> findByCongKhaiTrueAndKhoaFalse();
}
