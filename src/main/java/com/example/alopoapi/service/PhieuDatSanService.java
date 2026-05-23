package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Transactional
    public PhieuDatSanEntity createPhieuDat(
            CreatePhieuDatRequest request
    ) {

        var san = sanRepo.findByIdForUpdate(
                request.getMaSan()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Không tìm thấy sân"
                )
        );

        boolean isConflict =
                phieuDatSanRepo.existsConflict(
                        request.getMaSan(),
                        request.getBatDau(),
                        request.getKetThuc()
                );

        if (isConflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Khung giờ này vừa được đặt"
            );
        }

        long minutes = Duration.between(
                request.getBatDau(),
                request.getKetThuc()
        ).toMinutes();

        double soGio = minutes / 60.0;

        PhieuDatSanEntity newPD =
                new PhieuDatSanEntity();

        newPD.setMaPhieuDat(
                "PD_" + UUID.randomUUID()
        );

        newPD.setMaSan(
                request.getMaSan()
        );

        newPD.setMaNguoiDung(
                request.getMaNguoiDung()
        );

        newPD.setBatDau(
                request.getBatDau()
        );

        newPD.setKetThuc(
                request.getKetThuc()
        );

        newPD.setNgayLap(
                LocalDateTime.now()
        );

        newPD.setTongTien(
                san.getGia() * soGio
        );

        newPD.setTrangThai(
                "CHO_XAC_NHAN"
        );

        return phieuDatSanRepo.save(newPD);
    }

    public List<PhieuDatSanEntity> getPhieuDatForUser(String usernane) {

        List<PhieuDatSanEntity> pdList = new ArrayList<>();

        for (var pd : phieuDatSanRepo.findAll()) {
            if (pd.getMaNguoiDung().equals(usernane)) {
                pdList.add(pd);
            }
        }

        return pdList;
    }

    public void deletePhieuDat(String maPhieuDat) {

        phieuDatSanRepo.deleteById(maPhieuDat);
    }
}