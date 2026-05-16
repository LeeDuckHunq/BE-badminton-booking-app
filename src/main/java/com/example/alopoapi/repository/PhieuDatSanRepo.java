package com.example.alopoapi.repository;

import com.example.alopoapi.entity.PhieuDatSanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PhieuDatSanRepo extends JpaRepository<PhieuDatSanEntity, String> {

    List<PhieuDatSanEntity>
    findByBatDauBetween(
            LocalDateTime start,
            LocalDateTime end
    );

}
