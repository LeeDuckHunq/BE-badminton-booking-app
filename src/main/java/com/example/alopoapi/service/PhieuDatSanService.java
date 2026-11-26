package com.example.alopoapi.service;

import com.example.alopoapi.dto.SendMessageDTO;
import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.ThongBaoEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.HoaDonRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class PhieuDatSanService {

    static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    static final String DA_XAC_NHAN = "DA_XAC_NHAN";
    public static final String NGUON_APP = "APP";
    public static final String NGUON_TAI_SAN = "TAI_SAN";
    private static final Duration MAX_BOOKING_DURATION = Duration.ofHours(12);
    private static final Duration PAST_TOLERANCE = Duration.ofMinutes(5);
    private static final Duration WALK_IN_PAST_TOLERANCE = Duration.ofMinutes(30);
    /** Khách chỉ được xin huỷ đơn đã thanh toán / xác nhận trước giờ chơi ít nhất 30 phút. */
    public static final Duration CANCEL_NOTICE = Duration.ofMinutes(30);
    public static final String HUY_CHO_XU_LY = "CHO_XU_LY";
    public static final String HUY_DONG_Y = "DONG_Y";
    public static final String HUY_TU_CHOI = "TU_CHOI";

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

    private ThongBaoService thongBao;
    private ChatService chatService;

    @Autowired(required = false)
    void setThongBaoService(ThongBaoService thongBao) {
        this.thongBao = thongBao;
    }

    @Autowired(required = false)
    void setChatService(ChatService chatService) {
        this.chatService = chatService;
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

    /** Khách tự đặt trên app: chờ xác nhận, thanh toán qua hoá đơn + bill. */
    public PhieuDatSanEntity createPhieuDat(CreatePhieuDatRequest request, String username) {
        return reserve(request, PAST_TOLERANCE, booking -> {
            booking.setMaNguoiDung(username);
            booking.setNguonDat(NGUON_APP);
            booking.setTrangThai(CHO_XAC_NHAN);
        });
    }

    /**
     * Chủ sân / admin đặt hộ khách (gọi điện hoặc tới sân). Quyền với sân phải được kiểm tra trước khi gọi.
     * Cho phép đặt khung 30 phút đang diễn ra (khách tới sân chơi ngay).
     */
    public PhieuDatSanEntity createWalkIn(CreatePhieuDatRequest request, Consumer<PhieuDatSanEntity> details) {
        return reserve(request, WALK_IN_PAST_TOLERANCE, booking -> {
            booking.setNguonDat(NGUON_TAI_SAN);
            booking.setTrangThai(DA_XAC_NHAN);
            details.accept(booking);
        });
    }

    private PhieuDatSanEntity reserve(CreatePhieuDatRequest request, Duration pastTolerance, Consumer<PhieuDatSanEntity> customize) {
        validateRequest(request, pastTolerance);

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
            booking.setBatDau(request.getBatDau());
            booking.setKetThuc(request.getKetThuc());
            booking.setNgayLap(LocalDateTime.now());
            booking.setTongTien(PricingService.total(san, request.getBatDau(), request.getKetThuc()));
            customize.accept(booking);

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

    // ── Khách xin huỷ đơn đã thanh toán / đã xác nhận ─────────────────────────

    /**
     * Khách xin huỷ lượt đã thanh toán (đã gửi bill) hoặc đã được xác nhận, trước giờ chơi ít nhất 30 phút.
     * Hệ thống nhắn cho chủ sân trong app và gửi thông báo; chủ sân là người huỷ để nhả khung giờ.
     * Trả về thông tin liên hệ chủ sân (username, số điện thoại / link Zalo của cụm sân).
     */
    public Map<String, Object> requestCancel(String maPhieuDat, String username, String reason) {
        PhieuDatSanEntity booking = ownBooking(maPhieuDat, username);
        String text = reason == null ? "" : reason.trim();
        if (text.length() < 3) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng ghi lý do huỷ");
        if (text.length() > 300) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lý do tối đa 300 ký tự");
        boolean paid = hoaDonRepo.existsByMaPhieuDat(maPhieuDat);
        if (CHO_XAC_NHAN.equals(booking.getTrangThai()) && !paid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn chưa thanh toán: bạn có thể huỷ ngay ở bước thanh toán");
        }
        if (!DA_XAC_NHAN.equals(booking.getTrangThai()) && !CHO_XAC_NHAN.equals(booking.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn đã huỷ hoặc đã hoàn thành");
        }
        if (booking.getBatDau() == null || LocalDateTime.now().plus(CANCEL_NOTICE).isAfter(booking.getBatDau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ xin huỷ được trước giờ chơi ít nhất 30 phút. Vui lòng gọi trực tiếp cho chủ sân");
        }
        Map<String, Object> contact = contactOf(booking);
        if (HUY_CHO_XU_LY.equals(booking.getYeuCauHuy())) {
            return cancelResult(booking, contact);
        }
        booking.setYeuCauHuy(HUY_CHO_XU_LY);
        booking.setLyDoHuy(text);
        booking.setNgayYeuCauHuy(LocalDateTime.now());
        booking.setPhanHoiHuy(null);
        phieuDatSanRepo.save(booking);

        String owner = (String) contact.get("chuSan");
        String where = thongBao == null ? booking.getMaSan() : thongBao.describe(booking);
        String code = shortCode(booking.getMaPhieuDat());
        if (chatService != null && owner != null) {
            try {
                String room = chatService.getOrCreateRoom(username, owner);
                chatService.sendMessage(new SendMessageDTO(room, username,
                        "[Yêu cầu huỷ sân] Mình xin huỷ lượt " + where + " (đơn #" + code + "). Lý do: " + text
                                + ". Nhờ anh/chị xem giúp và hoàn tiền theo chính sách của sân ạ."), username, false);
            } catch (RuntimeException ignored) {
                // Không chặn yêu cầu huỷ nếu nhắn tin lỗi; chủ sân vẫn nhận thông báo.
            }
        }
        if (thongBao != null) {
            thongBao.notify(owner, ThongBaoEntity.YEU_CAU_HUY, "Khách xin huỷ sân",
                    thongBao.displayName(username) + " xin huỷ " + where + ". Lý do: " + text,
                    ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
        }
        return cancelResult(booking, contact);
    }

    /** Khách rút lại yêu cầu huỷ khi chủ sân chưa xử lý. */
    public PhieuDatSanEntity withdrawCancel(String maPhieuDat, String username) {
        PhieuDatSanEntity booking = ownBooking(maPhieuDat, username);
        if (!HUY_CHO_XU_LY.equals(booking.getYeuCauHuy())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không có yêu cầu huỷ đang chờ");
        }
        booking.setYeuCauHuy(null);
        booking.setNgayYeuCauHuy(null);
        phieuDatSanRepo.save(booking);
        if (thongBao != null) {
            thongBao.notify(thongBao.ownerOfCourt(booking.getMaSan()), ThongBaoEntity.YEU_CAU_HUY,
                    "Khách đã rút yêu cầu huỷ", thongBao.displayName(username) + " vẫn giữ lượt " + thongBao.describe(booking) + ".",
                    ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
        }
        return booking;
    }

    /** Thông tin liên hệ chủ sân của đơn (để khách nhắn trong app hoặc mở Zalo). */
    public Map<String, Object> contactOf(PhieuDatSanEntity booking) {
        Map<String, Object> contact = new LinkedHashMap<>();
        var court = booking.getMaSan() == null ? null : sanRepo.findById(booking.getMaSan()).orElse(null);
        CumSanEntity venue = court == null || court.getMaCumSan() == null ? null : cumSanRepo.findById(court.getMaCumSan()).orElse(null);
        contact.put("chuSan", venue == null ? null : venue.getOwnerUsername());
        contact.put("tenCumSan", venue == null ? null : venue.getTenCumSan());
        String phone = venue == null || venue.getsDT() == null ? "" : venue.getsDT().replaceAll("[^0-9+]", "");
        if (phone.startsWith("+84")) phone = "0" + phone.substring(3);
        contact.put("soDienThoai", phone.isBlank() ? null : phone);
        contact.put("zalo", phone.isBlank() ? null : "https://zalo.me/" + phone);
        return contact;
    }

    private Map<String, Object> cancelResult(PhieuDatSanEntity booking, Map<String, Object> contact) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("booking", booking);
        result.put("lienHe", contact);
        return result;
    }

    private PhieuDatSanEntity ownBooking(String maPhieuDat, String username) {
        PhieuDatSanEntity booking = phieuDatSanRepo.findById(maPhieuDat == null ? "" : maPhieuDat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu đặt"));
        if (booking.getMaNguoiDung() == null || !booking.getMaNguoiDung().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Đây không phải lượt đặt của bạn");
        }
        return booking;
    }

    // ── Chủ sân đổi sân ──────────────────────────────────────────────────────

    /** Chuyển lượt đặt sang sân khác (quyền của chủ sân đã được kiểm tra). Giữ nguyên giờ và giá. */
    public PhieuDatSanEntity moveCourt(PhieuDatSanEntity booking, SanEntity target, String reason) {
        if (!target.isTrangThai()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sân mới đang tạm ngưng hoạt động");
        }
        String lockOwner = UUID.randomUUID().toString();
        if (!bookingLockService.tryAcquire(target.getMaSan(), lockOwner)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sân đang được xử lý bởi một yêu cầu đặt khác");
        }
        try {
            if (phieuDatSanRepo.existsConflict(target.getMaSan(), booking.getBatDau(), booking.getKetThuc())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sân mới đã có lịch trong khung giờ này");
            }
            booking.setMaSanCu(booking.getMaSan());
            booking.setMaSan(target.getMaSan());
            booking.setLyDoDoiSan(reason);
            booking.setNgayDoiSan(LocalDateTime.now());
            return phieuDatSanRepo.save(booking);
        } finally {
            bookingLockService.release(target.getMaSan(), lockOwner);
        }
    }

    static String shortCode(String id) {
        String clean = id == null ? "" : id.replaceFirst("^PD_", "").replace("-", "");
        return clean.substring(0, Math.min(8, clean.length())).toUpperCase();
    }

    private void validateRequest(CreatePhieuDatRequest request, Duration pastTolerance) {
        if (request == null ||
                request.getMaSan() == null || request.getBatDau() == null ||
                request.getKetThuc() == null ||
                !request.getKetThuc().isAfter(request.getBatDau())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thông tin đặt sân không hợp lệ"
            );
        }
        if (request.getBatDau().isBefore(LocalDateTime.now().minus(pastTolerance))) {
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
