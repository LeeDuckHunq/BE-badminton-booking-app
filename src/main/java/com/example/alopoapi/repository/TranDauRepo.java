package com.example.alopoapi.repository;

import com.example.alopoapi.entity.nhom.TranDauEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TranDauRepo extends MongoRepository<TranDauEntity, String> {

    boolean existsByMaPhieuDatAndTrangThai(String maPhieuDat, String trangThai);

    List<TranDauEntity> findByMaPhieuDatInAndTrangThai(Collection<String> maPhieuDat, String trangThai);

    List<TranDauEntity> findByMaNhom(String maNhom);

    List<TranDauEntity> findByCongKhaiTrueAndTrangThaiAndBatDauAfter(String trangThai, LocalDateTime after);

    List<TranDauEntity> findByMaCumSanIn(Collection<String> maCumSan);

    long countByMaNhom(String maNhom);
}
