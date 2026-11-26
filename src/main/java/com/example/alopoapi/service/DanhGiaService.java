package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.DanhGiaEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.DanhGiaRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Đánh giá cụm sân sau khi chơi. */
@Service
public class DanhGiaService {

    private static final int MAX_LENGTH = 1000;

    private final DanhGiaRepo danhGiaRepo;
    private final PhieuDatSanRepo phieuDatSanRepo;
    private final SanRepo sanRepo;
    private final UserRepo userRepo;
    private final ManagementService managementService;

    public DanhGiaService(DanhGiaRepo danhGiaRepo, PhieuDatSanRepo phieuDatSanRepo, SanRepo sanRepo, UserRepo userRepo, ManagementService managementService) {
        this.danhGiaRepo = danhGiaRepo;
        this.phieuDatSanRepo = phieuDatSanRepo;
        this.sanRepo = sanRepo;
        this.userRepo = userRepo;
        this.managementService = managementService;
    }

    // ── Khách ────────────────────────────────────────────────────────────────

    /**
     * Khách đánh giá một lần chơi của chính mình (đơn Hoàn thành, hoặc Đã xác nhận và đã qua giờ chơi).
     * Gửi lại cho cùng phiếu đặt sẽ cập nhật đánh giá cũ (giữ nguyên trả lời của chủ sân).
     */
    public DanhGiaEntity submit(String username, String maPhieuDat, int soSao, String noiDung) {
        if (soSao < 1 || soSao > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số sao phải từ 1 đến 5");
        }
        String text = noiDung == null ? null : noiDung.trim();
        if (text != null && text.isEmpty()) text = null;
        if (text != null && text.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nhận xét tối đa " + MAX_LENGTH + " ký tự");
        }

        PhieuDatSanEntity booking = phieuDatSanRepo.findById(maPhieuDat == null ? "" : maPhieuDat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu đặt"));
        if (booking.getMaNguoiDung() == null || !booking.getMaNguoiDung().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ đánh giá được lần chơi của chính mình");
        }
        if (!canReview(booking)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ đánh giá được sau khi đã chơi xong");
        }
        SanEntity court = sanRepo.findById(booking.getMaSan())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sân"));

        LocalDateTime now = LocalDateTime.now();
        DanhGiaEntity review = danhGiaRepo.findById(reviewId(maPhieuDat)).orElseGet(() -> {
            DanhGiaEntity r = new DanhGiaEntity();
            r.setMaDanhGia(reviewId(maPhieuDat));
            r.setMaPhieuDat(maPhieuDat);
            r.setMaNguoiDung(booking.getMaNguoiDung());
            r.setNgayTao(now);
            return r;
        });
        review.setMaSan(court.getMaSan());
        review.setMaCumSan(court.getMaCumSan());
        review.setSoSao(soSao);
        review.setNoiDung(text);
        if (!now.equals(review.getNgayTao())) review.setNgayCapNhat(now);
        return danhGiaRepo.save(review);
    }

    public List<DanhGiaEntity> myReviews(String username) {
        return danhGiaRepo.findByMaNguoiDungOrderByNgayTaoDesc(username);
    }

    /** Công khai: điểm trung bình, phân bố sao và danh sách đánh giá (không lộ username). */
    public Map<String, Object> venueReviews(String maCumSan) {
        List<DanhGiaEntity> reviews = danhGiaRepo.findByMaCumSanAndAnFalseOrderByNgayTaoDesc(maCumSan);
        Map<String, String> courtNames = sanRepo.findByMaCumSan(maCumSan).stream()
                .collect(Collectors.toMap(SanEntity::getMaSan, SanEntity::getTenSan, (a, b) -> a));
        Map<String, Optional<UserEntity>> users = new HashMap<>();

        List<Map<String, Object>> items = reviews.stream().map(r -> {
            UserEntity u = users.computeIfAbsent(r.getMaNguoiDung(), userRepo::findByUsernameIgnoreCase).orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("maDanhGia", r.getMaDanhGia());
            item.put("tenNguoiDanhGia", displayName(u));
            item.put("soSao", r.getSoSao());
            item.put("noiDung", r.getNoiDung());
            item.put("tenSan", courtNames.get(r.getMaSan()));
            item.put("ngayTao", r.getNgayTao());
            item.put("daSua", r.getNgayCapNhat() != null);
            item.put("phanHoi", r.getPhanHoi());
            item.put("ngayPhanHoi", r.getNgayPhanHoi());
            return item;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>(summary(reviews));
        result.put("reviews", items);
        return result;
    }

    // ── Quản lý ──────────────────────────────────────────────────────────────

    /** Đánh giá các cụm sân thuộc quyền quản lý (admin: tất cả), kèm thông tin khách và lần chơi. */
    public List<Map<String, Object>> managedReviews(String venueId, Authentication authentication) {
        List<String> venueIds = managementService.venues(authentication).stream()
                .map(CumSanEntity::getMaCumSan)
                .filter(id -> venueId == null || venueId.isBlank() || id.equals(venueId))
                .toList();
        if (venueIds.isEmpty()) return List.of();
        Map<String, Optional<UserEntity>> users = new HashMap<>();
        return danhGiaRepo.findByMaCumSanInOrderByNgayTaoDesc(venueIds).stream().map(r -> {
            UserEntity u = users.computeIfAbsent(r.getMaNguoiDung(), userRepo::findByUsernameIgnoreCase).orElse(null);
            PhieuDatSanEntity booking = phieuDatSanRepo.findById(r.getMaPhieuDat()).orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("review", r);
            item.put("customerName", u != null && u.getFullName() != null ? u.getFullName() : r.getMaNguoiDung());
            item.put("batDau", booking == null ? null : booking.getBatDau());
            item.put("ketThuc", booking == null ? null : booking.getKetThuc());
            return item;
        }).toList();
    }

    private ThongBaoService thongBao;

    @Autowired(required = false)
    void setThongBaoService(ThongBaoService thongBao) {
        this.thongBao = thongBao;
    }

    /** Chủ sân / admin trả lời công khai. Nội dung rỗng = xoá trả lời. */
    public DanhGiaEntity reply(String id, String text, Authentication authentication) {
        DanhGiaEntity review = requireManaged(id, authentication);
        String reply = text == null ? null : text.trim();
        if (reply != null && reply.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trả lời tối đa " + MAX_LENGTH + " ký tự");
        }
        if (reply == null || reply.isEmpty()) {
            review.setPhanHoi(null);
            review.setNgayPhanHoi(null);
            review.setNguoiPhanHoi(null);
        } else {
            boolean first = review.getPhanHoi() == null;
            review.setPhanHoi(reply);
            review.setNgayPhanHoi(LocalDateTime.now());
            review.setNguoiPhanHoi(authentication.getName());
            if (first && thongBao != null) {
                thongBao.notify(review.getMaNguoiDung(), com.example.alopoapi.entity.ThongBaoEntity.DANH_GIA_PHAN_HOI,
                        "Chủ sân đã trả lời đánh giá của bạn", reply.length() > 140 ? reply.substring(0, 140) + "…" : reply,
                        com.example.alopoapi.entity.ThongBaoEntity.REVIEW, review.getMaCumSan());
            }
        }
        return danhGiaRepo.save(review);
    }

    /** Chỉ admin: ẩn / hiện đánh giá vi phạm. */
    public DanhGiaEntity setHidden(String id, boolean hidden) {
        DanhGiaEntity review = danhGiaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"));
        review.setAn(hidden);
        return danhGiaRepo.save(review);
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    static boolean canReview(PhieuDatSanEntity booking) {
        return "DA_HOAN_THANH".equals(booking.getTrangThai())
                || ("DA_XAC_NHAN".equals(booking.getTrangThai()) && booking.getKetThuc() != null && booking.getKetThuc().isBefore(LocalDateTime.now()));
    }

    static String reviewId(String maPhieuDat) {
        return "DG_" + maPhieuDat;
    }

    /** "Nguyễn Tuấn Anh" → "Tuấn Anh N."; không có tên → "Người chơi SmashZone". */
    static String displayName(UserEntity user) {
        if (user == null || user.getFullName() == null || user.getFullName().isBlank()) return "Người chơi SmashZone";
        String[] words = user.getFullName().trim().split("\\s+");
        if (words.length == 1) return words[0];
        if (words.length == 2) return words[1] + " " + words[0].charAt(0) + ".";
        return words[words.length - 2] + " " + words[words.length - 1] + " " + words[0].charAt(0) + ".";
    }

    private Map<String, Object> summary(List<DanhGiaEntity> reviews) {
        int[] distribution = new int[5];
        double total = 0;
        for (DanhGiaEntity r : reviews) {
            distribution[r.getSoSao() - 1]++;
            total += r.getSoSao();
        }
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("diemTrungBinh", reviews.isEmpty() ? null : Math.round(total / reviews.size() * 10) / 10.0);
        s.put("soDanhGia", reviews.size());
        Map<String, Integer> dist = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) dist.put(String.valueOf(star), distribution[star - 1]);
        s.put("phanBo", dist);
        return s;
    }

    private DanhGiaEntity requireManaged(String id, Authentication authentication) {
        DanhGiaEntity review = danhGiaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"));
        if (!managementService.canManageCourt(review.getMaSan(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không quản lý cụm sân này");
        }
        return review;
    }
}
