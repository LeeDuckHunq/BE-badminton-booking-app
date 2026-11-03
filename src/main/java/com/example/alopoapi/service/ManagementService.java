package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
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

    public ManagementService(CumSanRepo cumSanRepo, SanRepo sanRepo, PhieuDatSanRepo bookingRepo, UserRepo userRepo) {
        this.cumSanRepo = cumSanRepo;
        this.sanRepo = sanRepo;
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
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
        result.put("todayBookingCount", today);
        result.put("revenue", revenue);
        if (isAdmin(authentication)) {
            result.put("userCount", userRepo.count());
            result.put("ownerCount", userRepo.findAll().stream().filter(user -> "ROLE_OWNER".equals(normalizeRole(user.getRole()))).count());
        }
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
        court.setTrangThai(creating || request.isTrangThai());
        return sanRepo.save(court);
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
        UserEntity customer = userRepo.findByUsernameIgnoreCase(booking.getMaNguoiDung()).orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("booking", booking);
        result.put("court", court);
        result.put("venue", venue);
        result.put("customer", customer);
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
        booking.setTrangThai(normalized);
        return bookingRepo.save(booking);
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
