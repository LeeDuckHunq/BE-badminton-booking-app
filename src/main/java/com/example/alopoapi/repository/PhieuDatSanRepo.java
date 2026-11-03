package com.example.alopoapi.repository;

import com.example.alopoapi.entity.PhieuDatSanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuDatSanRepo extends MongoRepository<PhieuDatSanEntity, String> {

    List<PhieuDatSanEntity>
    findByBatDauBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<PhieuDatSanEntity>
    findByMaSan(String maSan);

    List<PhieuDatSanEntity>
    findByMaNguoiDung(String maNguoiDung);

    @Query(value = "{ 'maSan': ?0, " +
            "'trangThai': { '$in': ['CHO_XAC_NHAN', 'DA_XAC_NHAN'] }, " +
            "'batDau': { '$lt': ?2 }, 'ketThuc': { '$gt': ?1 } }", exists = true)
    boolean existsConflict(
            String maSan,
            LocalDateTime batDau,
            LocalDateTime ketThuc
    );
}
