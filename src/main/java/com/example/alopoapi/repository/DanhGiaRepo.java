package com.example.alopoapi.repository;

import com.example.alopoapi.entity.DanhGiaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface DanhGiaRepo extends MongoRepository<DanhGiaEntity, String> {
    List<DanhGiaEntity> findByMaCumSanAndAnFalseOrderByNgayTaoDesc(String maCumSan);

    List<DanhGiaEntity> findByMaCumSanInOrderByNgayTaoDesc(Collection<String> maCumSan);

    List<DanhGiaEntity> findByMaNguoiDungOrderByNgayTaoDesc(String maNguoiDung);

    List<DanhGiaEntity> findByAnFalse();
}
