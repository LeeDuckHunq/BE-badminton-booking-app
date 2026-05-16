package com.example.alopoapi.service;

import com.example.alopoapi.dto.CreatePhieuDatRequest;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Service
public class PhieuDatSanService {

    @Autowired
    private PhieuDatSanRepo phieuDatSanRepo;

    @Autowired
    private SanRepo sanRepo;

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

    public PhieuDatSanEntity createPhieuDat(CreatePhieuDatRequest request) {

        var san = sanRepo.findById(request.getMaSan());
        var soGio = Duration.between(
                request.getBatDau(),
                request.getKetThuc()
        ).toHours();

        PhieuDatSanEntity newPD = new PhieuDatSanEntity();

        newPD.setMaPhieuDat("PD00" + (phieuDatSanRepo.count() + 1));
        newPD.setMaSan(request.getMaSan());
        newPD.setMaNguoiDung(request.getMaNguoiDung());
        newPD.setMaKhuyenMai(request.getMaKhuyenMai());
        newPD.setBatDau(request.getBatDau());
        newPD.setKetThuc(request.getKetThuc());
        newPD.setNgayLap(LocalDateTime.now());
        newPD.setTongTien(san.get().getGia() * soGio);

        phieuDatSanRepo.save(newPD);

        return newPD;
    }
}