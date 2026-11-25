package com.example.alopoapi.repository;

import com.example.alopoapi.entity.nhom.ThanhVienNhomEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ThanhVienNhomRepo extends MongoRepository<ThanhVienNhomEntity, String> {

    List<ThanhVienNhomEntity> findByMaNhom(String maNhom);

    List<ThanhVienNhomEntity> findByMaNhomIn(Collection<String> maNhom);

    List<ThanhVienNhomEntity> findByUsername(String username);

    long countByMaNhomAndTrangThai(String maNhom, String trangThai);

    void deleteByMaNhom(String maNhom);
}
