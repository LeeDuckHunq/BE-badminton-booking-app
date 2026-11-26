package com.example.alopoapi.repository;

import com.example.alopoapi.entity.ThongBaoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThongBaoRepo extends MongoRepository<ThongBaoEntity, String> {

    List<ThongBaoEntity> findTop60ByNguoiNhanOrderByThoiGianDesc(String nguoiNhan);

    long countByNguoiNhanAndDaDocFalse(String nguoiNhan);

    List<ThongBaoEntity> findByNguoiNhanAndDaDocFalse(String nguoiNhan);

    List<ThongBaoEntity> findByNguoiNhanAndDaGuiFalseAndThoiGianAfterOrderByThoiGianAsc(String nguoiNhan, LocalDateTime after);

    Optional<ThongBaoEntity> findFirstByNguoiNhanAndLoaiAndNguoiGuiAndDaDocFalse(String nguoiNhan, String loai, String nguoiGui);

    void deleteByThoiGianBefore(LocalDateTime before);
}
