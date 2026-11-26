package com.example.alopoapi.repository;

import com.example.alopoapi.entity.KetBanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KetBanRepo extends MongoRepository<KetBanEntity, String> {

    List<KetBanEntity> findByNguoiGuiOrNguoiNhan(String nguoiGui, String nguoiNhan);
}
