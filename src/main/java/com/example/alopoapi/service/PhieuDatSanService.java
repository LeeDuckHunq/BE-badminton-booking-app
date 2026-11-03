package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class PhieuDatSanService {

    private final PhieuDatSanRepo phieuDatSanRepo;
    private final SanRepo sanRepo;
    private final BookingLockService bookingLockService;

    public PhieuDatSanService(
            PhieuDatSanRepo phieuDatSanRepo,
            SanRepo sanRepo,
            BookingLockService bookingLockService
    ) {
        this.phieuDatSanRepo = phieuDatSanRepo;
        this.sanRepo = sanRepo;
        this.bookingLockService = bookingLockService;
    }

    public List<PhieuDatSanEntity> getPhieuDatTheoNgay(LocalDate ngay) {
        LocalDateTime start = ngay.atStartOfDay();
        LocalDateTime end = ngay.atTime(LocalTime.MAX);
        return phieuDatSanRepo.findByBatDauBetween(start, end);
    }

    public PhieuDatSanEntity createPhieuDat(CreatePhieuDatRequest request) {
        validateRequest(request);

        var san = sanRepo.findById(request.getMaSan())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sân"
                ));

        String lockOwner = UUID.randomUUID().toString();
        if (!bookingLockService.tryAcquire(request.getMaSan(), lockOwner)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sân đang được xử lý bởi một yêu cầu đặt khác"
            );
        }

        try {
            if (phieuDatSanRepo.existsConflict(
                    request.getMaSan(),
                    request.getBatDau(),
                    request.getKetThuc()
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Khung giờ này vừa được đặt"
                );
            }

            long minutes = Duration.between(
                    request.getBatDau(),
                    request.getKetThuc()
            ).toMinutes();

            PhieuDatSanEntity booking = new PhieuDatSanEntity();
            booking.setMaPhieuDat("PD_" + UUID.randomUUID());
            booking.setMaSan(request.getMaSan());
            booking.setMaNguoiDung(request.getMaNguoiDung());
            booking.setBatDau(request.getBatDau());
            booking.setKetThuc(request.getKetThuc());
            booking.setNgayLap(LocalDateTime.now());
            booking.setTongTien(san.getGia() * (minutes / 60.0));
            booking.setTrangThai("CHO_XAC_NHAN");

            return phieuDatSanRepo.save(booking);
        } finally {
            bookingLockService.release(request.getMaSan(), lockOwner);
        }
    }

    public List<PhieuDatSanEntity> getPhieuDatForUser(String username) {
        return phieuDatSanRepo.findByMaNguoiDung(username);
    }

    public void deletePhieuDat(String maPhieuDat) {
        phieuDatSanRepo.deleteById(maPhieuDat);
    }

    private void validateRequest(CreatePhieuDatRequest request) {
        if (request == null || request.getMaNguoiDung() == null ||
                request.getMaSan() == null || request.getBatDau() == null ||
                request.getKetThuc() == null ||
                !request.getKetThuc().isAfter(request.getBatDau())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thông tin đặt sân không hợp lệ"
            );
        }
    }
}
