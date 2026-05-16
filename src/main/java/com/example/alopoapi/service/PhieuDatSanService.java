package com.example.alopoapi.service;

import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class PhieuDatSanService {

    @Autowired
    private PhieuDatSanRepo phieuDatSanRepo;

    public List<PhieuDatSanEntity>
    getPhieuDatTheoNgay(LocalDate ngay) {

        LocalDateTime start =
                ngay.atStartOfDay();

        LocalDateTime end =
                ngay.atTime(LocalTime.MAX);

        return phieuDatSanRepo
                .findByBatDauBetween(
                        start,
                        end
                );
    }
}