package com.example.alopoapi.repository;

import com.example.alopoapi.dto.TopCumSanDTO;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuDatSanRepo extends JpaRepository<PhieuDatSanEntity, String> {

    List<PhieuDatSanEntity>
    findByBatDauBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<PhieuDatSanEntity>
    findByMaSan(String maSan);

    long count();

    @Query("""
        SELECT new com.example.alopoapi.dto.TopCumSanDTO(
            s.maCumSan,
            COUNT(p.maPhieuDat)
        )
        FROM PhieuDatSanEntity p
        JOIN SanEntity s ON p.maSan = s.maSan
        GROUP BY s.maCumSan
        ORDER BY COUNT(p.maPhieuDat) DESC
        """)
    List<TopCumSanDTO> findTopCumSan(Pageable pageable);
}
