package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.HoaDonRepo;
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

    static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    private static final Duration MAX_BOOKING_DURATION = Duration.ofHours(12);
    private static final Duration PAST_TOLERANCE = Duration.ofMinutes(5);

    private final PhieuDatSanRepo phieuDatSanRepo;
    private final SanRepo sanRepo;
    private final CumSanRepo cumSanRepo;
    private final HoaDonRepo hoaDonRepo;
    private final BookingLockService bookingLockService;

    public PhieuDatSanService(
            PhieuDatSanRepo phieuDatSanRepo,
            SanRepo sanRepo,
            CumSanRepo cumSanRepo,
            HoaDonRepo hoaDonRepo,
            BookingLockService bookingLockService
    ) {
        this.phieuDatSanRepo = phieuDatSanRepo;
        this.sanRepo = sanRepo;
        this.cumSanRepo = cumSanRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.bookingLockService = bookingLockService;
    }

    /**
     * Lịch đặt công khai theo ngày (dùng để vẽ lưới giờ trống).
     * Không trả về người đặt để tránh lộ thông tin khách hàng.
     */
    public List<PhieuDatSanEntity> getPhieuDatTheoNgay(LocalDate ngay) {
        LocalDateTime start = ngay.atStartOfDay();
        LocalDateTime end = ngay.atTime(LocalTime.MAX);
        return phieuDatSanRepo.findByBatDauBetween(start, end).stream()
                .map(booking -> new PhieuDatSanEntity(
                        booking.getMaPhieuDat(),
                        null,
                        booking.getMaSan(),
                        booking.getBatDau(),
                        booking.getKetThuc(),
                        0,
                        null,
                        booking.getTrangThai()
                ))
                .toList();
    }

    public PhieuDatSanEntity createPhieuDat(CreatePhieuDatRequest request, String username) {
        validateRequest(request);

        var san = sanRepo.findById(request.getMaSan())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sân"
                ));
        if (!san.isTrangThai()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sân đang tạm ngưng hoạt động");
        }

        CumSanEntity cumSan = san.getMaCumSan() == null ? null : cumSanRepo.findById(san.getMaCumSan()).orElse(null);
        if (cumSan == null || !"ACTIVE".equals(cumSan.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cụm sân không còn hoạt động");
        }
        validateOpeningHours(cumSan, request);

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
            booking.setMaNguoiDung(username);
            booking.setBatDau(request.getBatDau());
            booking.setKetThuc(request.getKetThuc());
            booking.setNgayLap(LocalDateTime.now());
            booking.setTongTien(san.getGia() * (minutes / 60.0));
            booking.setTrangThai(CHO_XAC_NHAN);

            return phieuDatSanRepo.save(booking);
        } finally {
            bookingLockService.release(request.getMaSan(), lockOwner);
        }
    }

    public List<PhieuDatSanEntity> getPhieuDatForUser(String username) {
        return phieuDatSanRepo.findByMaNguoiDung(username);
    }

    /**
     * Khách chỉ được xoá phiếu của chính mình, khi phiếu còn chờ xác nhận và chưa có hoá đơn
     * (tức là huỷ ngay ở bước thanh toán).
     */
    public void deletePhieuDat(String maPhieuDat, String username) {
        PhieuDatSanEntity booking = phieuDatSanRepo.findById(maPhieuDat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu đặt"));
        if (booking.getMaNguoiDung() == null || !booking.getMaNguoiDung().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xoá phiếu đặt này");
        }
        if (!CHO_XAC_NHAN.equals(booking.getTrangThai()) || hoaDonRepo.existsByMaPhieuDat(maPhieuDat)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiếu đặt đã thanh toán hoặc đã được xử lý, không thể xoá");
        }
        phieuDatSanRepo.deleteById(maPhieuDat);
    }

    private void validateRequest(CreatePhieuDatRequest request) {
        if (request == null ||
                request.getMaSan() == null || request.getBatDau() == null ||
                request.getKetThuc() == null ||
                !request.getKetThuc().isAfter(request.getBatDau())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thông tin đặt sân không hợp lệ"
            );
        }
        if (request.getBatDau().isBefore(LocalDateTime.now().minus(PAST_TOLERANCE))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể đặt khung giờ trong quá khứ");
        }
        if (Duration.between(request.getBatDau(), request.getKetThuc()).compareTo(MAX_BOOKING_DURATION) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời lượng đặt sân quá dài");
        }
    }

    private void validateOpeningHours(CumSanEntity cumSan, CreatePhieuDatRequest request) {
        if (!request.getBatDau().toLocalDate().equals(request.getKetThuc().toLocalDate())
                && !request.getKetThuc().toLocalTime().equals(LocalTime.MIDNIGHT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ đặt được trong cùng một ngày");
        }
        LocalTime open = cumSan.getGioMoCua();
        LocalTime close = cumSan.getGioDongCua();
        if (open == null || close == null || !close.isAfter(open)) {
            return;
        }
        LocalTime start = request.getBatDau().toLocalTime();
        LocalTime end = request.getKetThuc().toLocalTime();
        boolean endsAtMidnight = end.equals(LocalTime.MIDNIGHT);
        if (start.isBefore(open) || endsAtMidnight || end.isAfter(close)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khung giờ nằm ngoài giờ mở cửa của cụm sân");
        }
    }
}
