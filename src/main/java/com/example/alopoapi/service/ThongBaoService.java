package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.ThongBaoEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.entity.nhom.ThamGiaTranEntity;
import com.example.alopoapi.entity.nhom.TranDauEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.ThamGiaTranRepo;
import com.example.alopoapi.repository.ThongBaoRepo;
import com.example.alopoapi.repository.TranDauRepo;
import com.example.alopoapi.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tạo và đọc thông báo. Các hàm tạo thông báo không bao giờ ném lỗi ra ngoài
 * để không làm hỏng nghiệp vụ chính (duyệt đơn, gửi tin nhắn…).
 */
@Service
public class ThongBaoService {

    private static final Logger log = LoggerFactory.getLogger(ThongBaoService.class);
    /** Điện thoại chỉ nhận lại thông báo chưa gửi trong khoảng này (tránh dồn thông báo cũ khi lâu không mở app). */
    static final long PUSH_WINDOW_HOURS = 24;
    private static final int MAX_PREVIEW = 140;
    private static final String[] THU = {"", "T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DM = DateTimeFormatter.ofPattern("dd/MM");

    private final ThongBaoRepo repo;
    private final SanRepo sanRepo;
    private final CumSanRepo cumSanRepo;
    private final UserRepo userRepo;
    private final TranDauRepo tranRepo;
    private final ThamGiaTranRepo thamGiaRepo;

    public ThongBaoService(ThongBaoRepo repo, SanRepo sanRepo, CumSanRepo cumSanRepo, UserRepo userRepo,
                           TranDauRepo tranRepo, ThamGiaTranRepo thamGiaRepo) {
        this.repo = repo;
        this.sanRepo = sanRepo;
        this.cumSanRepo = cumSanRepo;
        this.userRepo = userRepo;
        this.tranRepo = tranRepo;
        this.thamGiaRepo = thamGiaRepo;
    }

    // ── Đọc ─────────────────────────────────────────────────────────────────

    public Map<String, Object> list(String me) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("chuaDoc", repo.countByNguoiNhanAndDaDocFalse(me));
        result.put("items", repo.findTop60ByNguoiNhanOrderByThoiGianDesc(me));
        return result;
    }

    public Map<String, Object> unread(String me) {
        return Map.of("chuaDoc", repo.countByNguoiNhanAndDaDocFalse(me));
    }

    public Map<String, Object> markRead(String id, String me) {
        ThongBaoEntity tb = repo.findById(id)
                .filter(t -> t.getNguoiNhan().equalsIgnoreCase(me))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));
        if (!tb.isDaDoc()) {
            tb.setDaDoc(true);
            tb.setDaGui(true);
            repo.save(tb);
        }
        return unread(me);
    }

    public Map<String, Object> markAllRead(String me) {
        List<ThongBaoEntity> items = repo.findByNguoiNhanAndDaDocFalse(me);
        items.forEach(t -> { t.setDaDoc(true); t.setDaGui(true); });
        repo.saveAll(items);
        return unread(me);
    }

    /** Thông báo chưa đẩy lên điện thoại (trong 24 giờ gần nhất); đánh dấu đã gửi ngay khi trả về. */
    public List<ThongBaoEntity> takePending(String me) {
        List<ThongBaoEntity> items = repo.findByNguoiNhanAndDaGuiFalseAndThoiGianAfterOrderByThoiGianAsc(
                me, LocalDateTime.now().minusHours(PUSH_WINDOW_HOURS));
        items.forEach(t -> t.setDaGui(true));
        repo.saveAll(items);
        return items;
    }

    // ── Tạo ─────────────────────────────────────────────────────────────────

    public void notify(String to, String loai, String tieuDe, String noiDung, String doiTuong, String maDoiTuong) {
        if (to == null || to.isBlank()) return;
        try {
            ThongBaoEntity tb = new ThongBaoEntity();
            tb.setMaThongBao("TB_" + UUID.randomUUID());
            tb.setNguoiNhan(to);
            tb.setLoai(loai);
            tb.setTieuDe(tieuDe);
            tb.setNoiDung(noiDung);
            tb.setDoiTuong(doiTuong);
            tb.setMaDoiTuong(maDoiTuong);
            tb.setThoiGian(LocalDateTime.now());
            repo.save(tb);
        } catch (RuntimeException e) {
            log.warn("Không tạo được thông báo {} cho {}: {}", loai, to, e.getMessage());
        }
    }

    /** Tin nhắn mới: gộp vào thông báo chưa đọc của cùng người gửi. */
    public void message(String to, String from, String text) {
        if (to == null || from == null || to.equalsIgnoreCase(from)) return;
        try {
            String name = displayName(from);
            String preview = text.length() > MAX_PREVIEW ? text.substring(0, MAX_PREVIEW) + "…" : text;
            ThongBaoEntity tb = repo.findFirstByNguoiNhanAndLoaiAndNguoiGuiAndDaDocFalse(to, ThongBaoEntity.TIN_NHAN, from)
                    .orElse(null);
            if (tb == null) {
                tb = new ThongBaoEntity();
                tb.setMaThongBao("TB_" + UUID.randomUUID());
                tb.setNguoiNhan(to);
                tb.setLoai(ThongBaoEntity.TIN_NHAN);
                tb.setDoiTuong(ThongBaoEntity.CHAT);
                tb.setMaDoiTuong(from);
                tb.setNguoiGui(from);
                tb.setSoLuong(1);
            } else {
                tb.setSoLuong(tb.getSoLuong() + 1);
            }
            tb.setTieuDe(tb.getSoLuong() > 1 ? name + " (" + tb.getSoLuong() + " tin nhắn mới)" : name);
            tb.setNoiDung(preview);
            tb.setDaGui(false);
            tb.setThoiGian(LocalDateTime.now());
            repo.save(tb);
        } catch (RuntimeException e) {
            log.warn("Không tạo được thông báo tin nhắn cho {}: {}", to, e.getMessage());
        }
    }

    // ── Sự kiện đặt sân ─────────────────────────────────────────────────────

    /** Chủ sân / admin đổi trạng thái đơn → báo khách (và người chơi trong trận nếu đơn bị huỷ). */
    public void bookingStatusChanged(PhieuDatSanEntity booking, String oldStatus) {
        if (booking == null || booking.getTrangThai() == null || booking.getTrangThai().equals(oldStatus)) return;
        String where = describe(booking);
        switch (booking.getTrangThai()) {
            case "DA_XAC_NHAN" -> notify(booking.getMaNguoiDung(), ThongBaoEntity.DAT_SAN_XAC_NHAN,
                    "Đặt sân đã được xác nhận", where + ". Hẹn gặp bạn ở sân!", ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
            case "DA_HUY" -> {
                boolean requested = PhieuDatSanService.HUY_DONG_Y.equals(booking.getYeuCauHuy());
                String note = booking.getPhanHoiHuy() == null ? "" : " Chủ sân nhắn: " + booking.getPhanHoiHuy();
                notify(booking.getMaNguoiDung(), ThongBaoEntity.DAT_SAN_HUY,
                        requested ? "Chủ sân đã đồng ý huỷ sân" : "Đơn đặt sân đã bị huỷ",
                        where + (requested ? ". Khung giờ đã được nhả cho khách khác." : ". Liên hệ chủ sân nếu bạn cần hỗ trợ hoàn tiền.") + note,
                        ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
                cancelMatchesOf(booking);
            }
            case "DA_HOAN_THANH" -> notify(booking.getMaNguoiDung(), ThongBaoEntity.DAT_SAN_HOAN_THANH,
                    "Cảm ơn bạn đã ra sân!", where + ". Dành một phút đánh giá sân để giúp người chơi khác nhé.",
                    ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
            default -> { }
        }
    }

    /** Chủ sân chuyển lượt đặt sang sân khác → báo khách và người chơi trong trận gắn với lượt đặt. */
    public void courtChanged(PhieuDatSanEntity booking, SanEntity from, SanEntity to, String reason) {
        try {
            String body = "Lượt " + timeText(booking) + " được chuyển từ " + from.getTenSan() + " sang " + to.getTenSan()
                    + venueSuffix(to) + ". Lý do: " + reason + ". Giá giữ nguyên.";
            notify(booking.getMaNguoiDung(), ThongBaoEntity.DAT_SAN_DOI_SAN, "Sân của bạn đã được đổi", body,
                    ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
            for (TranDauEntity match : tranRepo.findByMaPhieuDatInAndTrangThai(List.of(booking.getMaPhieuDat()), TranDauEntity.MO)) {
                match.setMaSan(to.getMaSan());
                tranRepo.save(match);
                for (String user : players(match)) {
                    if (user.equalsIgnoreCase(booking.getMaNguoiDung())) continue;
                    notify(user, ThongBaoEntity.TRAN_DOI_SAN, "Trận \"" + match.getTieuDe() + "\" đổi sân", body,
                            ThongBaoEntity.MATCH, match.getMaTran());
                }
            }
        } catch (RuntimeException e) {
            log.warn("Không gửi được thông báo đổi sân {}: {}", booking.getMaPhieuDat(), e.getMessage());
        }
    }

    public void walkInLinked(PhieuDatSanEntity booking) {
        notify(booking.getMaNguoiDung(), ThongBaoEntity.DAT_HO, "Chủ sân đã đặt sân giúp bạn",
                describe(booking) + ". Đơn đã được xác nhận.", ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
    }

    /** Khách gửi bill → báo chủ cụm sân có đơn cần duyệt. */
    public void invoiceSubmitted(PhieuDatSanEntity booking, long amount) {
        String owner = ownerOfCourt(booking.getMaSan());
        notify(owner, ThongBaoEntity.DON_MOI, "Đơn mới chờ duyệt",
                displayName(booking.getMaNguoiDung()) + " đã chuyển khoản " + money(amount) + " cho " + describe(booking) + ".",
                ThongBaoEntity.BOOKING, booking.getMaPhieuDat());
    }

    // ── Sự kiện ghép trận ───────────────────────────────────────────────────

    public void matchEvent(TranDauEntity match, String to, String loai, String tieuDe, String noiDung) {
        if (match == null) return;
        notify(to, loai, tieuDe, noiDung, ThongBaoEntity.MATCH, match.getMaTran());
    }

    public void matchCancelled(TranDauEntity match, String reason) {
        try {
            for (String user : players(match)) {
                if (user.equalsIgnoreCase(match.getChuTran())) continue;
                notify(user, ThongBaoEntity.TRAN_HUY, "Trận \"" + match.getTieuDe() + "\" đã bị huỷ",
                        reason, ThongBaoEntity.MATCH, match.getMaTran());
            }
        } catch (RuntimeException e) {
            log.warn("Không gửi được thông báo huỷ trận {}: {}", match.getMaTran(), e.getMessage());
        }
    }

    // ── Tiện ích ────────────────────────────────────────────────────────────

    /** Đơn bị huỷ → trận đang mở gắn với đơn cũng huỷ theo (không còn sân để chơi). */
    private void cancelMatchesOf(PhieuDatSanEntity booking) {
        try {
            for (TranDauEntity match : tranRepo.findByMaPhieuDatInAndTrangThai(List.of(booking.getMaPhieuDat()), TranDauEntity.MO)) {
                match.setTrangThai(TranDauEntity.DA_HUY);
                tranRepo.save(match);
                matchCancelled(match, "Lượt đặt sân " + timeText(booking) + " của trận đã bị chủ sân huỷ nên trận cũng huỷ theo.");
            }
        } catch (RuntimeException e) {
            log.warn("Không huỷ được trận theo đơn {}: {}", booking.getMaPhieuDat(), e.getMessage());
        }
    }

    private List<String> players(TranDauEntity match) {
        return thamGiaRepo.findByMaTran(match.getMaTran()).stream()
                .filter(p -> ThamGiaTranEntity.THAM_GIA.equals(p.getTrangThai()) || ThamGiaTranEntity.CHO.equals(p.getTrangThai()))
                .map(ThamGiaTranEntity::getUsername)
                .toList();
    }

    public String ownerOfCourt(String maSan) {
        return sanRepo.findById(maSan == null ? "" : maSan)
                .flatMap(s -> cumSanRepo.findById(s.getMaCumSan() == null ? "" : s.getMaCumSan()))
                .map(CumSanEntity::getOwnerUsername)
                .orElse(null);
    }

    /** "Sân 2 · Cụm Sân Quận 1, 18:00–19:30 T6 27/11" */
    public String describe(PhieuDatSanEntity booking) {
        SanEntity court = booking.getMaSan() == null ? null : sanRepo.findById(booking.getMaSan()).orElse(null);
        String place = court == null ? "Sân" : court.getTenSan() + venueSuffix(court);
        return place + ", " + timeText(booking);
    }

    private String venueSuffix(SanEntity court) {
        if (court == null || court.getMaCumSan() == null) return "";
        return cumSanRepo.findById(court.getMaCumSan()).map(v -> " · " + v.getTenCumSan()).orElse("");
    }

    static String timeText(PhieuDatSanEntity booking) {
        if (booking.getBatDau() == null || booking.getKetThuc() == null) return "";
        return booking.getBatDau().format(HM) + "–" + booking.getKetThuc().format(HM) + " "
                + THU[booking.getBatDau().getDayOfWeek().getValue()] + " " + booking.getBatDau().format(DM);
    }

    public String displayName(String username) {
        if (username == null) return "Khách";
        return userRepo.findByUsernameIgnoreCase(username)
                .map(UserEntity::getFullName)
                .filter(n -> n != null && !n.isBlank())
                .orElse(username);
    }

    private static String money(long amount) {
        return String.format("%,d", amount).replace(',', '.') + " đ";
    }
}
