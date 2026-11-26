package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.dto.request.WalkInBookingRequest;
import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.HoaDonRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ManagementService {

    private final CumSanRepo cumSanRepo;
    private final SanRepo sanRepo;
    private final PhieuDatSanRepo bookingRepo;
    private final UserRepo userRepo;
    private final HoaDonRepo hoaDonRepo;
    private final PhieuDatSanService phieuDatSanService;

    private static final List<String> PAYMENT_METHODS = List.of("TIEN_MAT", "CHUYEN_KHOAN", "TRA_SAU");

    public ManagementService(CumSanRepo cumSanRepo, SanRepo sanRepo, PhieuDatSanRepo bookingRepo, UserRepo userRepo,
                             HoaDonRepo hoaDonRepo, PhieuDatSanService phieuDatSanService) {
        this.cumSanRepo = cumSanRepo;
        this.sanRepo = sanRepo;
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.phieuDatSanService = phieuDatSanService;
    }

    private ThongBaoService thongBao;

    @Autowired(required = false)
    void setThongBaoService(ThongBaoService thongBao) {
        this.thongBao = thongBao;
    }

    // ── Đặt hộ khách ─────────────────────────────────────────────────────────

    /** Chủ sân / admin đặt hộ khách: đơn xác nhận ngay, ghi cách thanh toán. */
    public PhieuDatSanEntity createWalkInBooking(WalkInBookingRequest request, Authentication authentication) {
        if (request == null || request.getMaSan() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn sân");
        }
        requireCourt(request.getMaSan(), authentication);

        String payment = required(request.getPhuongThucThanhToan(), "Vui lòng chọn cách thanh toán").toUpperCase(Locale.ROOT);
        if (!PAYMENT_METHODS.contains(payment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cách thanh toán không hợp lệ");
        }
        String note = blankToNull(request.getGhiChu());
        if (note != null && note.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ghi chú tối đa 500 ký tự");
        }

        String username = blankToNull(request.getMaNguoiDung());
        UserEntity customer = null;
        if (username != null) {
            customer = userRepo.findByUsernameIgnoreCase(username)
                    .filter(UserEntity::isActive)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản khách hàng"));
        }
        String name = blankToNull(request.getTenKhachHang());
        String phone = blankToNull(request.getSdtKhachHang());
        if (customer != null) {
            if (name == null) name = customer.getFullName() != null ? customer.getFullName() : customer.getUsername();
            if (phone == null) phone = customer.getPhoneNumber();
        } else {
            if (name == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên khách");
            if (phone == null || !phone.replaceAll("[\\s.]", "").matches("\\+?\\d{9,12}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại khách không hợp lệ");
            }
        }
        if (name.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên khách quá dài");
        }

        CreatePhieuDatRequest booking = new CreatePhieuDatRequest();
        booking.setMaSan(request.getMaSan());
        booking.setBatDau(request.getBatDau());
        booking.setKetThuc(request.getKetThuc());

        final UserEntity linked = customer;
        final String guestName = name;
        final String guestPhone = phone;
        PhieuDatSanEntity created = phieuDatSanService.createWalkIn(booking, b -> {
            b.setMaNguoiDung(linked == null ? null : linked.getUsername());
            b.setTenKhachHang(guestName);
            b.setSdtKhachHang(guestPhone);
            b.setPhuongThucThanhToan(payment);
            b.setGhiChu(note);
            b.setNguoiTao(authentication.getName());
        });
        if (thongBao != null && linked != null) {
            thongBao.walkInLinked(created);
        }
        return created;
    }

    /**
     * Tìm khách có tài khoản để gắn vào đơn đặt hộ. Chỉ khớp theo tên / username (≥ 3 ký tự)
     * hoặc đúng số điện thoại đầy đủ; số điện thoại trả về được che bớt để tránh dò danh bạ.
     */
    public List<Map<String, Object>> searchCustomers(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String digits = q.replaceAll("\\D", "");
        boolean byPhone = digits.length() >= 9 && digits.length() == q.replaceAll("[\\s.+]", "").length();
        if (!byPhone && q.length() < 3) {
            return List.of();
        }
        return userRepo.findAll().stream()
                .filter(UserEntity::isActive)
                .filter(u -> byPhone
                        ? u.getPhoneNumber() != null && u.getPhoneNumber().replaceAll("\\D", "").endsWith(digits.length() > 10 ? digits.substring(digits.length() - 9) : digits)
                        : u.getUsername().toLowerCase(Locale.ROOT).contains(q)
                          || (u.getFullName() != null && u.getFullName().toLowerCase(Locale.ROOT).contains(q)))
                .limit(8)
                .map(u -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("username", u.getUsername());
                    item.put("fullName", u.getFullName());
                    item.put("phoneMasked", maskPhone(u.getPhoneNumber()));
                    return item;
                })
                .toList();
    }

    /** Lịch hoạt động trong 1 ngày của các cụm sân được quản lý (hoặc 1 cụm nếu chỉ định). */
    public Map<String, Object> timeline(LocalDate date, String venueId, Authentication authentication) {
        List<CumSanEntity> venues = venueId == null || venueId.isBlank()
                ? venues(authentication).stream().filter(v -> "ACTIVE".equals(v.getTrangThai())).toList()
                : List.of(requireVenue(venueId, authentication));
        List<String> venueIds = venues.stream().map(CumSanEntity::getMaCumSan).toList();
        List<SanEntity> courts = venueIds.isEmpty() ? List.of() : sanRepo.findByMaCumSanIn(venueIds);
        List<String> courtIds = courts.stream().map(SanEntity::getMaSan).toList();
        List<PhieuDatSanEntity> bookings = courtIds.isEmpty() ? List.of()
                : bookingRepo.findByMaSanInAndBatDauBetween(courtIds, date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX));

        Map<String, UserEntity> users = new LinkedHashMap<>();
        for (PhieuDatSanEntity b : bookings) {
            if (b.getMaNguoiDung() != null && !users.containsKey(b.getMaNguoiDung())) {
                userRepo.findByUsernameIgnoreCase(b.getMaNguoiDung()).ifPresent(u -> users.put(b.getMaNguoiDung(), u));
            }
        }
        List<Map<String, Object>> items = bookings.stream()
                .sorted(Comparator.comparing(PhieuDatSanEntity::getBatDau))
                .map(b -> {
                    UserEntity u = users.get(b.getMaNguoiDung());
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("booking", b);
                    item.put("customerName", b.getTenKhachHang() != null ? b.getTenKhachHang()
                            : u != null && u.getFullName() != null ? u.getFullName() : b.getMaNguoiDung());
                    item.put("customerPhone", b.getSdtKhachHang() != null ? b.getSdtKhachHang() : u != null ? u.getPhoneNumber() : null);
                    item.put("hasInvoice", hoaDonRepo.existsByMaPhieuDat(b.getMaPhieuDat()));
                    return item;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date.toString());
        result.put("venues", venues);
        result.put("courts", courts);
        result.put("bookings", items);
        return result;
    }

    private String maskPhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 6) return "***";
        return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 3);
    }

    public Map<String, Object> currentUser(Authentication authentication) {
        UserEntity user = userRepo.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", user.getUsername());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("phoneNumber", user.getPhoneNumber());
        result.put("role", role(authentication));
        return result;
    }

    public Map<String, Object> overview(Authentication authentication) {
        List<CumSanEntity> venues = venues(authentication);
        List<SanEntity> courts = courts(authentication);
        List<PhieuDatSanEntity> bookings = bookings(authentication);
        double revenue = bookings.stream()
                .filter(booking -> !"DA_HUY".equals(booking.getTrangThai()))
                .mapToDouble(PhieuDatSanEntity::getTongTien)
                .sum();
        long pending = bookings.stream().filter(booking -> "CHO_XAC_NHAN".equals(booking.getTrangThai())).count();
        long today = bookings.stream()
                .filter(booking -> booking.getBatDau() != null && booking.getBatDau().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("venueCount", venues.size());
        result.put("courtCount", courts.size());
        result.put("bookingCount", bookings.size());
        result.put("pendingCount", pending);
        result.put("cancelRequestCount", bookings.stream()
                .filter(booking -> PhieuDatSanService.HUY_CHO_XU_LY.equals(booking.getYeuCauHuy()) && !"DA_HUY".equals(booking.getTrangThai()))
                .count());
        result.put("todayBookingCount", today);
        result.put("revenue", revenue);
        if (isAdmin(authentication)) {
            result.put("userCount", userRepo.count());
            result.put("ownerCount", userRepo.findAll().stream().filter(user -> "ROLE_OWNER".equals(normalizeRole(user.getRole()))).count());
        }
        result.put("revenueByDay", revenueByDay(bookings, 7));
        result.put("recentBookings", bookings.stream()
                .sorted(Comparator.comparing(PhieuDatSanEntity::getNgayLap, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList());
        return result;
    }

    public List<CumSanEntity> venues(Authentication authentication) {
        if (isAdmin(authentication)) {
            return cumSanRepo.findAll();
        }
        return cumSanRepo.findByOwnerUsername(authentication.getName());
    }

    public CumSanEntity saveVenue(CumSanEntity request, Authentication authentication, String id) {
        boolean creating = id == null;
        CumSanEntity venue = creating
                ? new CumSanEntity()
                : requireVenue(id, authentication);

        venue.setMaCumSan(creating ? generatedId("CS") : id);
        venue.setTenCumSan(required(request.getTenCumSan(), "Tên cụm sân là bắt buộc"));
        venue.setDiaChi(required(request.getDiaChi(), "Địa chỉ là bắt buộc"));
        venue.setMoTa(request.getMoTa());
        venue.setsDT(request.getsDT());
        venue.setGioMoCua(request.getGioMoCua());
        venue.setGioDongCua(request.getGioDongCua());
        venue.setTrangThai(request.getTrangThai() == null ? "ACTIVE" : request.getTrangThai());

        if (isAdmin(authentication)) {
            venue.setOwnerUsername(blankToNull(request.getOwnerUsername()));
        } else {
            venue.setOwnerUsername(authentication.getName());
        }
        return cumSanRepo.save(venue);
    }

    public void deleteVenue(String id, Authentication authentication) {
        CumSanEntity venue = requireVenue(id, authentication);
        venue.setTrangThai("DELETED");
        cumSanRepo.save(venue);
        sanRepo.findByMaCumSan(id).forEach(court -> {
            court.setTrangThai(false);
            sanRepo.save(court);
        });
    }

    public List<SanEntity> courts(Authentication authentication) {
        if (isAdmin(authentication)) {
            return sanRepo.findAll();
        }
        List<String> venueIds = venues(authentication).stream().map(CumSanEntity::getMaCumSan).toList();
        return venueIds.isEmpty() ? List.of() : sanRepo.findByMaCumSanIn(venueIds);
    }

    public SanEntity saveCourt(SanEntity request, Authentication authentication, String id) {
        requireVenue(request.getMaCumSan(), authentication);
        boolean creating = id == null;
        SanEntity court = creating ? new SanEntity() : requireCourt(id, authentication);
        court.setMaSan(creating ? generatedId("S") : id);
        court.setMaCumSan(request.getMaCumSan());
        court.setTenSan(required(request.getTenSan(), "Tên sân là bắt buộc"));
        if (request.getGia() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá sân không hợp lệ");
        }
        court.setGia(request.getGia());
        if (creating || request.bangGiaProvided()) {
            court.setBangGia(PricingService.normalize(request.getBangGia()));
        }
        court.setTrangThai(creating || request.isTrangThai());
        return sanRepo.save(court);
    }

    /** Áp dụng bảng giá của một sân cho các sân khác (cùng cụm) mà mình quản lý. */
    public List<SanEntity> copyPricing(String id, List<String> targets, boolean includeBasePrice, Authentication authentication) {
        SanEntity source = requireCourt(id, authentication);
        if (targets == null || targets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chọn ít nhất một sân để áp dụng");
        }
        List<SanEntity> updated = new java.util.ArrayList<>();
        for (String targetId : targets) {
            if (targetId == null || targetId.equals(id)) continue;
            SanEntity target = requireCourt(targetId, authentication);
            if (!java.util.Objects.equals(target.getMaCumSan(), source.getMaCumSan())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ áp dụng được cho sân trong cùng cụm sân");
            }
            target.setBangGia(PricingService.normalize(source.getBangGia()));
            if (includeBasePrice) target.setGia(source.getGia());
            updated.add(target);
        }
        return sanRepo.saveAll(updated);
    }

    public void deleteCourt(String id, Authentication authentication) {
        SanEntity court = requireCourt(id, authentication);
        court.setTrangThai(false);
        sanRepo.save(court);
    }

    public List<PhieuDatSanEntity> bookings(Authentication authentication) {
        if (isAdmin(authentication)) {
            return bookingRepo.findAll();
        }
        List<String> courtIds = courts(authentication).stream().map(SanEntity::getMaSan).toList();
        return courtIds.isEmpty() ? List.of() : bookingRepo.findByMaSanIn(courtIds);
    }

    public Map<String, Object> bookingDetail(String id, Authentication authentication) {
        PhieuDatSanEntity booking = bookingRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đặt"));
        SanEntity court = requireCourt(booking.getMaSan(), authentication);
        CumSanEntity venue = cumSanRepo.findById(court.getMaCumSan()).orElse(null);
        // Đơn đặt hộ khách vãng lai không gắn tài khoản.
        UserEntity customer = booking.getMaNguoiDung() == null ? null
                : userRepo.findByUsernameIgnoreCase(booking.getMaNguoiDung()).orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("booking", booking);
        result.put("court", court);
        result.put("venue", venue);
        result.put("customer", customer);
        result.put("invoice", hoaDonRepo.findFirstByMaPhieuDat(id).orElse(null));
        result.put("durationMinutes", booking.getBatDau() != null && booking.getKetThuc() != null
                ? Duration.between(booking.getBatDau(), booking.getKetThuc()).toMinutes()
                : 0);
        return result;
    }

    public PhieuDatSanEntity updateBookingStatus(String id, String status, Authentication authentication) {
        String normalized = required(status, "Trạng thái là bắt buộc").toUpperCase(Locale.ROOT);
        if (!List.of("CHO_XAC_NHAN", "DA_XAC_NHAN", "DA_HOAN_THANH", "DA_HUY").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái đơn đặt không hợp lệ");
        }
        PhieuDatSanEntity booking = bookingRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đặt"));
        requireCourt(booking.getMaSan(), authentication);
        String old = booking.getTrangThai();
        booking.setTrangThai(normalized);
        if ("DA_HUY".equals(normalized) && PhieuDatSanService.HUY_CHO_XU_LY.equals(booking.getYeuCauHuy())) {
            booking.setYeuCauHuy(PhieuDatSanService.HUY_DONG_Y);
        }
        PhieuDatSanEntity saved = bookingRepo.save(booking);
        if (thongBao != null) {
            thongBao.bookingStatusChanged(saved, old);
        }
        return saved;
    }

    /**
     * Chủ sân xử lý yêu cầu huỷ của khách: đồng ý → huỷ đơn, nhả khung giờ; từ chối → giữ đơn, ghi lý do.
     */
    public PhieuDatSanEntity handleCancelRequest(String id, boolean accept, String note, Authentication authentication) {
        PhieuDatSanEntity booking = bookingRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đặt"));
        requireCourt(booking.getMaSan(), authentication);
        if (!PhieuDatSanService.HUY_CHO_XU_LY.equals(booking.getYeuCauHuy())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn này không có yêu cầu huỷ đang chờ");
        }
        String text = blankToNull(note);
        if (text != null && text.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ghi chú tối đa 300 ký tự");
        }
        if (!accept && text == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng ghi lý do từ chối để khách hiểu");
        }
        booking.setPhanHoiHuy(text);
        if (accept) {
            String old = booking.getTrangThai();
            booking.setYeuCauHuy(PhieuDatSanService.HUY_DONG_Y);
            booking.setTrangThai("DA_HUY");
            PhieuDatSanEntity saved = bookingRepo.save(booking);
            if (thongBao != null) thongBao.bookingStatusChanged(saved, old);
            return saved;
        }
        booking.setYeuCauHuy(PhieuDatSanService.HUY_TU_CHOI);
        PhieuDatSanEntity saved = bookingRepo.save(booking);
        if (thongBao != null) {
            thongBao.notify(saved.getMaNguoiDung(), com.example.alopoapi.entity.ThongBaoEntity.HUY_BI_TU_CHOI,
                    "Chủ sân chưa đồng ý huỷ", thongBao.describe(saved) + ". Chủ sân nhắn: " + text,
                    com.example.alopoapi.entity.ThongBaoEntity.BOOKING, saved.getMaPhieuDat());
        }
        return saved;
    }

    /**
     * Đổi sân cho một lượt đặt (sân cũ có sự cố): sân mới phải cùng cụm, còn trống đúng khung giờ.
     * Giờ chơi và tiền giữ nguyên; khách và người chơi trong trận được báo.
     */
    public PhieuDatSanEntity changeCourt(String id, String newCourtId, String reason, Authentication authentication) {
        PhieuDatSanEntity booking = bookingRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đặt"));
        SanEntity from = requireCourt(booking.getMaSan(), authentication);
        SanEntity to = requireCourt(required(newCourtId, "Vui lòng chọn sân mới"), authentication);
        String why = required(reason, "Vui lòng ghi lý do đổi sân");
        if (why.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lý do tối đa 200 ký tự");
        }
        if (from.getMaSan().equals(to.getMaSan())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sân mới phải khác sân hiện tại");
        }
        if (from.getMaCumSan() == null || !from.getMaCumSan().equals(to.getMaCumSan())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ đổi được sang sân trong cùng cụm sân");
        }
        if (!List.of("CHO_XAC_NHAN", "DA_XAC_NHAN").contains(booking.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ đổi sân cho đơn đang chờ hoặc đã xác nhận");
        }
        if (booking.getKetThuc() == null || !booking.getKetThuc().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lượt chơi đã kết thúc");
        }
        PhieuDatSanEntity saved = phieuDatSanService.moveCourt(booking, to, why);
        if (thongBao != null) {
            thongBao.courtChanged(saved, from, to, why);
        }
        return saved;
    }

    /** Doanh thu và số đơn theo ngày lập đơn trong {@code days} ngày gần nhất (không tính đơn đã huỷ). */
    private List<Map<String, Object>> revenueByDay(List<PhieuDatSanEntity> bookings, int days) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            List<PhieuDatSanEntity> ofDay = bookings.stream()
                    .filter(b -> !"DA_HUY".equals(b.getTrangThai()))
                    .filter(b -> b.getNgayLap() != null && b.getNgayLap().toLocalDate().equals(day))
                    .toList();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.toString());
            point.put("revenue", ofDay.stream().mapToDouble(PhieuDatSanEntity::getTongTien).sum());
            point.put("count", ofDay.size());
            series.add(point);
        }
        return series;
    }

    /** ADMIN, hoặc OWNER sở hữu cụm sân chứa sân này. */
    public boolean canManageCourt(String maSan, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_OWNER".equals(authority.getAuthority()))) {
            return false;
        }
        if (maSan == null) {
            return false;
        }
        return sanRepo.findById(maSan)
                .filter(court -> court.getMaCumSan() != null)
                .flatMap(court -> cumSanRepo.findById(court.getMaCumSan()))
                .map(venue -> authentication.getName().equalsIgnoreCase(venue.getOwnerUsername()))
                .orElse(false);
    }

    private CumSanEntity requireVenue(String id, Authentication authentication) {
        CumSanEntity venue = cumSanRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy cụm sân"));
        if (!isAdmin(authentication) && !authentication.getName().equalsIgnoreCase(venue.getOwnerUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền quản lý cụm sân này");
        }
        return venue;
    }

    private SanEntity requireCourt(String id, Authentication authentication) {
        SanEntity court = sanRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sân"));
        requireVenue(court.getMaCumSan(), authentication);
        return court;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String role(Authentication authentication) {
        return authentication.getAuthorities().stream().findFirst().map(authority -> authority.getAuthority()).orElse("ROLE_USER");
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return "ROLE_USER";
        String normalized = role.toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }

    private String generatedId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
