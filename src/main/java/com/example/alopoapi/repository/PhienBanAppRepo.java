package com.example.alopoapi.repository;

import com.example.alopoapi.entity.PhienBanAppEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhienBanAppRepo extends MongoRepository<PhienBanAppEntity, String> {

    Optional<PhienBanAppEntity> findTopByNenTangOrderBySoBuildDesc(String nenTang);

    List<PhienBanAppEntity> findByNenTangOrderBySoBuildDesc(String nenTang);
}
