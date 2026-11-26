package com.example.alopoapi.service;

import com.example.alopoapi.dto.SendMessageDTO;
import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.DanhGiaEntity;
import com.example.alopoapi.entity.HinhAnhSanEntity;
import com.example.alopoapi.entity.HoaDonEntity;
import com.example.alopoapi.entity.KhungGia;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.entity.voucher.KhuyenMaiEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.DanhGiaRepo;
import com.example.alopoapi.repository.HinhAnhSanRepo;
import com.example.alopoapi.repository.HoaDonRepo;
import com.example.alopoapi.repository.KhuyenMaiRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.UserRepo;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Sinh / xoá dữ liệu demo trên máy chủ (chỉ admin gọi qua /management/admin/demo-data).
 * <ul>
 *   <li>Chỉ BỔ SUNG chỗ còn thiếu: không sửa tài khoản, đơn đặt, đánh giá có sẵn của người dùng thật.</li>
 *   <li>Mọi thứ tạo ra được ghi lại trong collection {@code DemoData} để xoá sạch bằng một lệnh.</li>
 *   <li>Đơn đặt demo có mã như đơn thật; danh sách mã được lưu trong {@code DemoData} để xoá.</li>
 * </ul>
 */
@Service
public class DemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);
    private static final String MARKER = "DemoData";
    private static final String MARKER_ID = "demo";
    static final String OWNER_PASSWORD = "owner1234";
    static final String CUSTOMER_PASSWORD = "secret12";

    private static final String IMG = "https://images.unsplash.com/%s?w=1200&q=70&auto=format&fit=crop";
    private static final List<String> PHOTOS = List.of("photo-1626224583764-f87db24ac4ea", "photo-1613918431703-aa50889e3be8",
            "photo-1521537634581-0dced2fee2ef", "photo-1599474924187-334a4ae5bd3c");
    private static final String BILL = String.format(IMG, "photo-1556742049-0cfed4f6a45d");

    private static final String[][] OWNERS = {
            {"hoanganh", "Hoàng Văn Anh", "0988777666"},
            {"tanbinh", "Cụm Sân Tân Bình", "0283999888"},
            {"ngocthao", "Nguyễn Ngọc Thảo", "0907111222"},
            {"quanglong", "Trần Quang Long", "0918222333"},
            {"minhchau", "Lê Minh Châu", "0938333444"},
            {"ductrong", "Phạm Đức Trọng", "0977444555"},
    };
    private static final String[][] CUSTOMERS = {
            {"tuananh", "Nguyễn Tuấn Anh", "0908123456"},
            {"minhkhang", "Lê Minh Khang", "0912345678"},
            {"mailinh", "Trần Mai Linh", "0933222111"},
            {"quochuy", "Phạm Quốc Huy", "0909456789"},
            {"thanhtruc", "Võ Thanh Trúc", "0937111222"},
            {"ducminh", "Đỗ Đức Minh", "0918333444"},
            {"baongoc", "Lê Bảo Ngọc", "0903555666"},
            {"giahuy", "Võ Gia Huy", "0904666777"},
            {"thuytien", "Đặng Thủy Tiên", "0905777888"},
            {"hoangphuc", "Ngô Hoàng Phúc", "0906888999"},
    };
    private static final String[] REVIEW_TEXTS = {
            "Sân sạch, thảm bám tốt, đèn sáng đều. Sẽ quay lại!",
            "Giá hợp lý, chủ sân thân thiện, có nước miễn phí.",
            "Giờ cao điểm hơi đông nhưng đặt trước trên app nên không phải chờ.",
            "Sân thoáng, có máy lạnh, chỗ để xe rộng rãi.",
            "Đèn hơi chói ở sân 1, còn lại ổn.",
            "Đặt sân nhanh, chủ sân duyệt bill trong vài phút.",
            "Thảm mới, lưới căng chuẩn, đánh rất đã.",
            "Vị trí dễ tìm, gần nhà, giá giờ sáng rất rẻ.",
    };
    private static final String[] OWNER_REPLIES = {
            "Cảm ơn bạn đã ủng hộ, hẹn gặp lại bạn ở sân nhé!",
            "Cảm ơn góp ý, sân đã thay đèn mới cho sân 1 rồi ạ.",
            "Rất vui vì bạn hài lòng. Cuối tuần sân có khung giờ ưu đãi nha!",
    };
    private static final String[] WALK_IN_NAMES = {"Anh Long", "Chị Hạnh", "Anh Tú (khách quen)", "Nhóm văn phòng FPT", "Anh Khoa", "Chị Vy"};

    @Autowired private MongoTemplate mongo;
    @Autowired private UserRepo userRepo;
    @Autowired private CumSanRepo cumSanRepo;
    @Autowired private SanRepo sanRepo;
    @Autowired private HinhAnhSanRepo imageRepo;
    @Autowired private KhuyenMaiRepo voucherRepo;
    @Autowired private PhieuDatSanRepo bookingRepo;
    @Autowired private HoaDonRepo invoiceRepo;
    @Autowired private DanhGiaRepo reviewRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PhieuDatSanService bookingService;
    @Autowired private ManagementService managementService;
    @Autowired private DanhGiaService reviewService;
    @Autowired private FavoriteService favoriteService;
    @Autowired private KhuyenMaiService voucherService;
    @Autowired private BanBeService friendService;
    @Autowired private ChatService chatService;
    @Autowired private NhomService groupService;
    @Autowired private TranDauService matchService;
    @Autowired private ThongBaoService notifyService;

    private final Random rnd = new Random(2026);

    // ── Trạng thái ──────────────────────────────────────────────────────────

    public Map<String, Object> status() {
        Document marker = mongo.findById(MARKER_ID, Document.class, MARKER);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("daTao", marker != null);
        if (marker != null) {
            result.put("ngayTao", marker.get("ngayTao"));
            result.put("thongKe", marker.get("thongKe"));
        }
        return result;
    }

    // ── Sinh dữ liệu ────────────────────────────────────────────────────────

    public synchronized Map<String, Object> generate() {
        if (mongo.findById(MARKER_ID, Document.class, MARKER) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đã có dữ liệu demo. Xoá dữ liệu demo cũ trước khi tạo lại.");
        }
        Track t = new Track();
        try {
            users(t);
            venues(t);
            vouchers(t);
            bookings(t);
            social(t);
        } finally {
            // Luôn ghi lại những gì đã tạo (kể cả khi lỗi giữa chừng) để còn xoá được.
            saveMarker(t);
        }
        return status();
    }

    private void users(Track t) {
        for (String[] o : OWNERS) createUser(t, o, OWNER_PASSWORD, "ROLE_OWNER");
        for (String[] c : CUSTOMERS) createUser(t, c, CUSTOMER_PASSWORD, "ROLE_USER");
    }

    private void createUser(Track t, String[] info, String password, String role) {
        if (userRepo.findByUsernameIgnoreCase(info[0]).isPresent()) return; // không đụng tài khoản đã có
        UserEntity u = new UserEntity(info[0], passwordEncoder.encode(password), role, info[1], info[0] + "@smashzone.demo", info[2]);
        u.setActive(true);
        userRepo.save(u);
        t.users.add(info[0]);
        t.count("taiKhoan");
    }

    /** Chủ sân, mô tả, số điện thoại, ảnh, sân, bảng giá cho các cụm sân còn thiếu. */
    private void venues(Track t) {
        List<String> owners = new ArrayList<>();
        for (String[] o : OWNERS) owners.add(o[0]);
        List<CumSanEntity> venues = cumSanRepo.findAll();
        venues.sort((a, b) -> a.getMaCumSan().compareTo(b.getMaCumSan()));
        int i = 0;
        for (CumSanEntity v : venues) {
            boolean changed = false;
            if (v.getOwnerUsername() == null || v.getOwnerUsername().isBlank()) {
                v.setOwnerUsername(owners.get(i % owners.size()));
                t.venueOwners.add(v.getMaCumSan());
                changed = true;
            }
            if (v.getMoTa() == null || v.getMoTa().isBlank()) {
                v.setMoTa(description(v));
                t.venueDescriptions.add(v.getMaCumSan());
                changed = true;
            }
            if (v.getsDT() == null || v.getsDT().isBlank()) {
                v.setsDT("0283" + String.format("%06d", 100000 + i * 1371));
                t.venuePhones.add(v.getMaCumSan());
                changed = true;
            }
            if (changed) cumSanRepo.save(v);
            if (imageRepo.findByMaCumSan(v.getMaCumSan()).isEmpty()) {
                String id = "IMG_DEMO_" + v.getMaCumSan();
                imageRepo.save(new HinhAnhSanEntity(id, String.format(IMG, PHOTOS.get(i % PHOTOS.size())), v.getMaCumSan()));
                t.images.add(id);
                t.count("anh");
            }
            List<SanEntity> courts = sanRepo.findByMaCumSan(v.getMaCumSan());
            double base = 80000 + (i % 5) * 10000;
            if (courts.isEmpty()) {
                for (int k = 1; k <= 3; k++) {
                    SanEntity c = new SanEntity("S_DEMO_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), "Sân " + k, base, true, v.getMaCumSan());
                    sanRepo.save(c);
                    t.courts.add(c.getMaSan());
                    t.count("san");
                }
                courts = sanRepo.findByMaCumSan(v.getMaCumSan());
            }
            for (SanEntity c : courts) {
                boolean save = false;
                if (c.getGia() <= 0) {
                    c.setGia(base);
                    save = true;
                }
                if (c.getBangGia() == null || c.getBangGia().isEmpty()) {
                    c.setBangGia(PricingService.normalize(priceTable(c.getGia())));
                    t.pricedCourts.add(c.getMaSan());
                    t.count("bangGia");
                    save = true;
                }
                if (save) sanRepo.save(c);
            }
            i++;
        }
        t.count("cumSanBoSung", t.venueOwners.size());
    }

    private static List<KhungGia> priceTable(double gia) {
        List<Integer> week = List.of(1, 2, 3, 4, 5), weekend = List.of(6, 7), all = List.of(1, 2, 3, 4, 5, 6, 7);
        double low = Math.max(50000, round5k(gia - 20000));
        return new ArrayList<>(List.of(
                new KhungGia(all, "05:00", "07:00", low, "Sáng sớm"),
                new KhungGia(week, "17:00", "21:00", round5k(gia + 30000), "Giờ cao điểm"),
                new KhungGia(weekend, "07:00", "21:00", round5k(gia + 20000), "Cuối tuần")
        ));
    }

    private static double round5k(double v) {
        return Math.round(v / 5000.0) * 5000.0;
    }

    private static String description(CumSanEntity v) {
        return v.getTenCumSan() + " có sân thảm PVC chuẩn thi đấu, đèn LED chống chói, có máy lạnh khu chờ, chỗ để xe rộng, "
                + "nước uống miễn phí và cho thuê vợt. Nhận đặt sân theo giờ, có giá ưu đãi buổi sáng và khung giờ cố định theo tháng.";
    }

    private void vouchers(Track t) {
        LocalDateTime now = LocalDateTime.now();
        Object[][] list = {
                {"SZ10", "Ưu đãi giờ thấp điểm (05:00 – 09:00)", 0.10, -3, 30},
                {"WEEKEND20", "Combo đánh đôi cuối tuần", 0.20, -1, 14},
                {"NEWBIE15", "Chào mừng thành viên mới SmashZone", 0.15, -5, 60},
                {"BWF25", "Gói luyện tập tiêu chuẩn BWF", 0.25, -1, 25},
        };
        for (Object[] v : list) {
            String id = (String) v[0];
            if (voucherRepo.existsById(id)) continue;
            voucherRepo.save(new KhuyenMaiEntity(id, (String) v[1], null, (Double) v[2], now.plusDays((Integer) v[3]), now.plusDays((Integer) v[4])));
            t.vouchers.add(id);
            t.count("voucher");
        }
        for (String[] c : CUSTOMERS) {
            if (!t.users.contains(c[0])) continue;
            safe(() -> voucherService.nhanKhuyenMai(c[0], "NEWBIE15"));
            if (rnd.nextBoolean()) safe(() -> voucherService.nhanKhuyenMai(c[0], "SZ10"));
        }
    }

    // ── Đặt sân ─────────────────────────────────────────────────────────────

    private record Slot(SanEntity court, CumSanEntity venue, LocalDateTime start, LocalDateTime end) { }

    private void bookings(Track t) {
        List<CumSanEntity> venues = cumSanRepo.findAll().stream().filter(v -> "ACTIVE".equals(v.getTrangThai())).toList();
        Map<String, List<SanEntity>> courts = new HashMap<>();
        for (CumSanEntity v : venues) {
            List<SanEntity> list = sanRepo.findByMaCumSan(v.getMaCumSan()).stream().filter(SanEntity::isTrangThai).toList();
            if (!list.isEmpty()) courts.put(v.getMaCumSan(), list);
        }
        List<CumSanEntity> usable = venues.stream().filter(v -> courts.containsKey(v.getMaCumSan())).toList();
        if (usable.isEmpty()) return;
        LocalDate today = LocalDate.now();

        // Tài khoản trong kịch bản demo (tuananh, minhkhang, mailinh) đặt sân ở cụm của hoanganh để chủ sân này có đơn chờ duyệt, yêu cầu huỷ…
        List<CumSanEntity> hoang = usable.stream().filter(v -> "hoanganh".equalsIgnoreCase(v.getOwnerUsername())).toList();
        List<String> featured = List.of("tuananh", "minhkhang", "mailinh");
        List<String> customers = new ArrayList<>();
        for (String[] c : CUSTOMERS) if (t.users.contains(c[0])) customers.add(c[0]);

        for (int ci = 0; ci < customers.size(); ci++) {
            String user = customers.get(ci);
            // 3 lượt đã chơi xong trong 3 tuần qua (kèm hoá đơn, phần lớn có đánh giá)
            for (int k = 0; k < 3; k++) {
                Slot s = slot(usable, courts, today.minusDays(2 + rnd.nextInt(19)), false);
                if (s == null) continue;
                PhieuDatSanEntity b = insert(t, s, user, "DA_HOAN_THANH", s.start.minusDays(1 + rnd.nextInt(3)));
                invoice(b);
                if (rnd.nextInt(10) < 7) review(t, b, s.venue);
            }
            // 1 lượt sắp tới đã xác nhận + 1 chờ duyệt (đã gửi bill)
            List<CumSanEntity> pool = featured.contains(user) && !hoang.isEmpty() ? hoang : usable;
            Slot up = slot(pool, courts, today.plusDays(2 + rnd.nextInt(4)), false);
            if (up != null) {
                PhieuDatSanEntity b = insert(t, up, user, "DA_XAC_NHAN", LocalDateTime.now().minusDays(rnd.nextInt(6)).minusHours(rnd.nextInt(8)));
                invoice(b);
                t.upcoming.put(user, b);
                notifyService.bookingStatusChanged(b, "CHO_XAC_NHAN");
            }
            if (ci % 2 == 0 || featured.contains(user)) {
                Slot p = slot(pool, courts, today.plusDays(1 + rnd.nextInt(4)), false);
                if (p != null) {
                    PhieuDatSanEntity b = insert(t, p, user, "CHO_XAC_NHAN", LocalDateTime.now().minusHours(1 + rnd.nextInt(20)));
                    HoaDonEntity hd = invoice(b);
                    notifyService.invoiceSubmitted(b, Math.round(hd.getTongTien()));
                }
            }
            // Thêm 1 lượt tối nay / ngày mai để lịch sân hôm nay có dữ liệu
            Slot soon = slot(usable, courts, ci % 2 == 0 ? today : today.plusDays(1), true);
            if (soon != null) {
                PhieuDatSanEntity b = insert(t, soon, user, "DA_XAC_NHAN", LocalDateTime.now().minusDays(1));
                invoice(b);
            }
            if (ci % 3 == 1) {
                Slot c = slot(usable, courts, today.minusDays(1 + rnd.nextInt(5)), false);
                if (c != null) insert(t, c, user, "DA_HUY", c.start.minusDays(2));
            }
        }

        // Đặt hộ khách vãng lai hôm nay / ngày mai cho mỗi cụm sân có chủ
        for (int i = 0; i < Math.min(usable.size(), 12); i++) {
            CumSanEntity v = usable.get(i);
            for (int k = 0; k < 2; k++) {
                Slot s = slot(List.of(v), courts, today.plusDays(k), true);
                if (s == null) continue;
                PhieuDatSanEntity b = base(s, "DA_XAC_NHAN", LocalDateTime.now().minusHours(2 + rnd.nextInt(30)));
                b.setNguonDat(PhieuDatSanService.NGUON_TAI_SAN);
                b.setTenKhachHang(WALK_IN_NAMES[(i + k) % WALK_IN_NAMES.length]);
                b.setSdtKhachHang("09" + (10000000 + rnd.nextInt(89999999)));
                b.setPhuongThucThanhToan(List.of("TIEN_MAT", "CHUYEN_KHOAN", "TRA_SAU").get(rnd.nextInt(3)));
                b.setNguoiTao(v.getOwnerUsername());
                bookingRepo.save(b);
                t.bookings.add(b.getMaPhieuDat());
                t.count("datHo");
            }
        }

        // Kịch bản demo: một yêu cầu huỷ đang chờ và một lượt đã đổi sân
        PhieuDatSanEntity cancel = t.upcoming.get("tuananh");
        if (cancel != null) safe(() -> bookingService.requestCancel(cancel.getMaPhieuDat(), "tuananh", "Thay đổi lịch trình, xin lỗi anh/chị"));
        PhieuDatSanEntity move = t.upcoming.get("minhkhang");
        if (move != null) {
            SanEntity from = sanRepo.findById(move.getMaSan()).orElse(null);
            if (from != null) {
                CumSanEntity venue = cumSanRepo.findById(from.getMaCumSan()).orElse(null);
                for (SanEntity other : sanRepo.findByMaCumSan(from.getMaCumSan())) {
                    if (other.getMaSan().equals(from.getMaSan()) || venue == null) continue;
                    var auth = new UsernamePasswordAuthenticationToken(venue.getOwnerUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
                    try {
                        managementService.changeCourt(move.getMaPhieuDat(), other.getMaSan(), "Sân cũ đang thay thảm", auth);
                        t.count("doiSan");
                        break;
                    } catch (RuntimeException ignored) {
                        // sân này bận, thử sân khác
                    }
                }
            }
        }
    }

    /** Chọn một khung giờ trống trong ngày (giờ mở cửa của cụm sân). {@code evening}: ưu tiên chiều tối. */
    private Slot slot(List<CumSanEntity> venues, Map<String, List<SanEntity>> courts, LocalDate day, boolean evening) {
        for (int attempt = 0; attempt < 25; attempt++) {
            CumSanEntity v = venues.get(rnd.nextInt(venues.size()));
            List<SanEntity> list = courts.get(v.getMaCumSan());
            if (list == null || list.isEmpty()) continue;
            SanEntity court = list.get(rnd.nextInt(list.size()));
            LocalTime open = v.getGioMoCua() == null ? LocalTime.of(6, 0) : v.getGioMoCua();
            LocalTime close = v.getGioDongCua() == null || !v.getGioDongCua().isAfter(open) ? LocalTime.of(22, 0) : v.getGioDongCua();
            int minutes = (2 + rnd.nextInt(3)) * 30; // 1h – 2h
            int from = open.getHour() * 60 + open.getMinute() + 30;
            int to = close.getHour() * 60 + close.getMinute() - minutes;
            if (evening) from = Math.max(from, 17 * 60);
            if (day.equals(LocalDate.now())) {
                LocalTime now = LocalTime.now();
                from = Math.max(from, now.getHour() * 60 + now.getMinute() + 60);
            }
            if (to <= from) continue;
            int start = from + rnd.nextInt((to - from) / 30 + 1) * 30;
            start = (start / 30) * 30;
            LocalDateTime s = day.atTime(start / 60, start % 60), e = s.plusMinutes(minutes);
            if (e.toLocalTime().isAfter(close) || !e.toLocalDate().equals(day)) continue;
            if (bookingRepo.existsConflict(court.getMaSan(), s, e)) continue;
            return new Slot(court, v, s, e);
        }
        return null;
    }

    private PhieuDatSanEntity base(Slot s, String status, LocalDateTime createdAt) {
        PhieuDatSanEntity b = new PhieuDatSanEntity("PD_" + UUID.randomUUID(), null, s.court.getMaSan(), s.start, s.end,
                PricingService.total(s.court, s.start, s.end), createdAt, status);
        b.setNguonDat(PhieuDatSanService.NGUON_APP);
        return b;
    }

    private PhieuDatSanEntity insert(Track t, Slot s, String user, String status, LocalDateTime createdAt) {
        PhieuDatSanEntity b = base(s, status, createdAt);
        b.setMaNguoiDung(user);
        bookingRepo.save(b);
        t.bookings.add(b.getMaPhieuDat());
        t.count("datSan");
        return b;
    }

    private HoaDonEntity invoice(PhieuDatSanEntity b) {
        HoaDonEntity hd = new HoaDonEntity("HD_" + b.getMaPhieuDat(), b.getMaPhieuDat(), null, Math.round(b.getTongTien()), b.getNgayLap().plusMinutes(10), BILL);
        return invoiceRepo.save(hd);
    }

    private void review(Track t, PhieuDatSanEntity b, CumSanEntity venue) {
        int stars = new int[]{5, 5, 4, 5, 4, 3, 5, 4}[rnd.nextInt(8)];
        try {
            DanhGiaEntity r = reviewService.submit(b.getMaNguoiDung(), b.getMaPhieuDat(), stars, REVIEW_TEXTS[rnd.nextInt(REVIEW_TEXTS.length)]);
            r.setNgayTao(b.getKetThuc().plusHours(2));
            r.setNgayCapNhat(null);
            if (rnd.nextInt(10) < 4 && venue.getOwnerUsername() != null) {
                r.setPhanHoi(OWNER_REPLIES[rnd.nextInt(OWNER_REPLIES.length)]);
                r.setNgayPhanHoi(b.getKetThuc().plusHours(6));
                r.setNguoiPhanHoi(venue.getOwnerUsername());
            }
            reviewRepo.save(r);
            t.count("danhGia");
        } catch (RuntimeException e) {
            log.warn("Không tạo được đánh giá demo: {}", e.getMessage());
        }
    }

    // ── Cộng đồng: yêu thích, bạn bè, chat, nhóm, trận ──────────────────────

    private void social(Track t) {
        List<String> venueIds = cumSanRepo.findAll().stream().map(CumSanEntity::getMaCumSan).sorted().toList();
        for (String[] c : CUSTOMERS) {
            if (!t.users.contains(c[0]) || venueIds.isEmpty()) continue;
            for (int k = 0; k < 2; k++) {
                String v = venueIds.get(rnd.nextInt(venueIds.size()));
                safe(() -> favoriteService.addFavForUser(c[0], v));
            }
        }
        if (!t.users.containsAll(List.of("tuananh", "minhkhang", "mailinh", "quochuy", "thanhtruc", "ducminh"))) return;

        // Bạn bè (đã kết bạn + lời mời đang chờ)
        String[][] friends = {{"minhkhang", "tuananh"}, {"mailinh", "tuananh"}, {"giahuy", "baongoc"}, {"thuytien", "mailinh"}, {"hoangphuc", "quochuy"}, {"ducminh", "giahuy"}};
        for (String[] f : friends) {
            safe(() -> friendService.request(f[0], f[1]));
            safe(() -> friendService.accept(f[1], f[0]));
        }
        safe(() -> friendService.request("quochuy", "tuananh"));
        safe(() -> friendService.request("tuananh", "thanhtruc"));
        safe(() -> friendService.request("baongoc", "tuananh"));

        // Chat khách ↔ chủ sân và bạn bè
        String ownerOfTa = ownerOfUpcoming(t, "tuananh", "hoanganh");
        chat("tuananh", ownerOfTa, new String[][]{
                {ownerOfTa, "Chào Tuấn Anh! Lượt đặt của bạn mình đã duyệt rồi nhé."},
                {"tuananh", "Dạ em cảm ơn anh! Sân có cho thuê vợt không ạ?"},
                {ownerOfTa, "Có bạn, 20k/cây/buổi. Nước suối miễn phí nha."},
                {"tuananh", "Vâng em cảm ơn anh nhiều, hẹn gặp anh ở sân 🏸"},
        });
        chat("minhkhang", "tuananh", new String[][]{{"minhkhang", "Tối thứ 5 này đánh đôi không ông?"}, {"tuananh", "Ok luôn, 7h nhé!"}});
        chat("baongoc", ownerOfUpcoming(t, "baongoc", "tanbinh"), new String[][]{{"baongoc", "Chị ơi sân còn trống tối thứ 7 không ạ?"}, {ownerOfUpcoming(t, "baongoc", "tanbinh"), "Còn em nhé, khung 19h–21h sân 2 đang trống."}});
        chat("giahuy", "baongoc", new String[][]{{"giahuy", "Mai đánh đơn với mình không?"}});
        t.count("hoiThoai", 4);

        // Nhóm
        Map<String, Object> g1 = group("minhkhang", "CLB Cầu Lông Quận 1 Chill", "Đánh đôi giao lưu tối thứ 3, 5, 7. Vui vẻ là chính, không cay cú 😄", "Quận 1, TP.HCM", "TB", true, 0);
        Map<String, Object> g2 = group("mailinh", "Hội Smash Tân Bình", "Nhóm kín cho anh em TB+ trở lên, đánh nhanh – smash mạnh.", "Tân Bình, TP.HCM", "TB_KHA", false, 1);
        Map<String, Object> g3 = group("ducminh", "Team Giao Lưu Thủ Đức", "Dành cho người mới và trình yếu – TB. Có người hướng dẫn kỹ thuật cơ bản.", "Thủ Đức, TP.HCM", "YEU", true, 2);
        Map<String, Object> g4 = group("giahuy", "Cầu Lông Văn Phòng Gò Vấp", "Đánh sau giờ làm 18h–20h các ngày trong tuần.", "Gò Vấp, TP.HCM", "TB", true, 3);
        joinAll(g1, "tuananh", "mailinh", "quochuy", "baongoc");
        joinAll(g2, "thanhtruc", "hoangphuc");
        joinAll(g3, "thuytien", "giahuy");
        joinAll(g4, "baongoc", "hoangphuc", "ducminh");
        if (g1 != null) {
            String id = (String) g1.get("maNhom");
            safe(() -> groupService.setRole(id, "minhkhang", "mailinh", "PHO_NHOM"));
            for (String[] m : new String[][]{{"minhkhang", "Tối thứ 5 sân Quận 1 nhé mọi người!"}, {"tuananh", "Em vào slot nha 🙌"}, {"mailinh", "Nhớ mang cầu nha, lần trước hết cầu giữa chừng 😂"}, {"quochuy", "Mình mang 1 ống Thành Công nhé"}}) {
                safe(() -> groupService.send(id, m[0], m[1]));
            }
        }
        if (g3 != null) safe(() -> groupService.join((String) g3.get("maNhom"), "hoangphuc", null, "Cho mình vào học hỏi với ạ"));

        // Trận ghép từ lượt sắp tới của chủ trận
        match(t, "minhkhang", g1, "Kèo đôi tối – Quận 1", "DOI", 4, List.of("tuananh", "mailinh"), "thanhtruc");
        match(t, "mailinh", null, "Tìm 2 bạn đánh đôi TB+", "DOI", 4, List.of("quochuy"), "hoangphuc");
        match(t, "ducminh", g3, "Giao lưu xoay vòng cho người mới", "GIAO_LUU", 8, List.of("thuytien", "giahuy"), null);
        match(t, "giahuy", g4, "Đánh đơn sau giờ làm", "DON", 2, List.of(), "baongoc");
    }

    private String ownerOfUpcoming(Track t, String user, String fallback) {
        PhieuDatSanEntity b = t.upcoming.get(user);
        if (b == null) return fallback;
        String owner = notifyService.ownerOfCourt(b.getMaSan());
        return owner == null ? fallback : owner;
    }

    private void chat(String a, String b, String[][] messages) {
        try {
            String room = chatService.getOrCreateRoom(a, b);
            for (String[] m : messages) chatService.sendMessage(new SendMessageDTO(room, m[0], m[1]), m[0]);
        } catch (RuntimeException e) {
            log.warn("Không tạo được chat demo {} - {}: {}", a, b, e.getMessage());
        }
    }

    private Map<String, Object> group(String owner, String name, String desc, String area, String level, boolean open, int color) {
        Map<String, Object> body = new HashMap<>();
        body.put("tenNhom", name);
        body.put("moTa", desc);
        body.put("khuVuc", area);
        body.put("trinhDo", level);
        body.put("congKhai", open);
        body.put("mau", color);
        try {
            return groupService.create(owner, body);
        } catch (RuntimeException e) {
            log.warn("Không tạo được nhóm demo {}: {}", name, e.getMessage());
            return null;
        }
    }

    private void joinAll(Map<String, Object> group, String... users) {
        if (group == null) return;
        String code = (String) group.get("maMoi");
        for (String u : users) safe(() -> groupService.joinByCode(u, code));
    }

    private void match(Track t, String host, Map<String, Object> group, String title, String type, int max, List<String> players, String requester) {
        PhieuDatSanEntity b = t.upcoming.get(host);
        if (b == null || "CHO_XU_LY".equals(bookingRepo.findById(b.getMaPhieuDat()).map(PhieuDatSanEntity::getYeuCauHuy).orElse(null))) return;
        Map<String, Object> body = new HashMap<>();
        body.put("maPhieuDat", b.getMaPhieuDat());
        if (group != null) body.put("maNhom", group.get("maNhom"));
        body.put("tieuDe", title);
        body.put("ghiChu", "Đánh vui, không tính điểm. Ai đến sớm khởi động trước nhé!");
        body.put("kieuChoi", type);
        body.put("soNguoiToiDa", max);
        body.put("trinhDoTu", "TB");
        body.put("trinhDoDen", "KHA");
        body.put("congKhai", true);
        body.put("nganHang", "MB");
        body.put("soTaiKhoan", "0901234567");
        body.put("chuTaiKhoan", host.toUpperCase());
        try {
            Map<String, Object> m = matchService.create(host, body);
            String id = (String) m.get("maTran");
            for (String p : players) {
                safe(() -> matchService.join(id, p, Map.of("trinhDo", "TB")));
                safe(() -> matchService.pay(id, p, Map.of("billUrl", BILL)));
            }
            if (!players.isEmpty()) safe(() -> matchService.confirmPayment(id, host, players.get(0), true));
            if (requester != null) safe(() -> matchService.join(id, requester, Map.of("trinhDo", "TB_KHA", "loiNhan", "Cho mình xin 1 slot nhé!")));
            t.count("tranGhep");
        } catch (RuntimeException e) {
            log.warn("Không tạo được trận demo {}: {}", title, e.getMessage());
        }
    }

    // ── Xoá dữ liệu demo ────────────────────────────────────────────────────

    public synchronized Map<String, Object> clear() {
        Document m = mongo.findById(MARKER_ID, Document.class, MARKER);
        if (m == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chưa có dữ liệu demo");
        List<String> users = list(m, "users");
        Map<String, Object> removed = new LinkedHashMap<>();

        // Đơn do bộ sinh tạo + mọi đơn tài khoản demo tự đặt sau đó (khi trình diễn).
        List<String> bookings = new ArrayList<>(list(m, "bookings"));
        if (!users.isEmpty()) {
            mongo.findDistinct(new Query(Criteria.where("maNguoiDung").in(users)), "_id", "PhieuDatSan", Object.class)
                    .forEach(id -> bookings.add(String.valueOf(id)));
        }
        removed.put("datSan", mongo.remove(new Query(Criteria.where("_id").in(bookings)), "PhieuDatSan").getDeletedCount());
        mongo.remove(new Query(Criteria.where("maPhieuDat").in(bookings)), "HoaDon");
        removed.put("danhGia", mongo.remove(new Query(Criteria.where("maPhieuDat").in(bookings)), "DanhGia").getDeletedCount());

        if (!users.isEmpty()) {
            List<Object> matchIds = mongo.findDistinct(new Query(Criteria.where("chuTran").in(users)), "_id", "TranDau", Object.class);
            mongo.remove(new Query(new Criteria().orOperator(Criteria.where("maTran").in(matchIds), Criteria.where("username").in(users))), "ThamGiaTran");
            removed.put("tranGhep", mongo.remove(new Query(Criteria.where("_id").in(matchIds)), "TranDau").getDeletedCount());

            List<Object> groupIds = mongo.findDistinct(new Query(Criteria.where("nguoiTao").in(users)), "_id", "Nhom", Object.class);
            mongo.remove(new Query(new Criteria().orOperator(Criteria.where("maNhom").in(groupIds), Criteria.where("username").in(users))), "ThanhVienNhom");
            List<String> groupRooms = groupIds.stream().map(id -> "NHOM_" + id).toList();
            mongo.remove(new Query(Criteria.where("maPhongChat").in(groupRooms)), "TinNhan");
            mongo.remove(new Query(Criteria.where("maPhongChat").in(groupRooms)), "ThanhVienPhongChat");
            mongo.remove(new Query(Criteria.where("_id").in(groupRooms)), "PhongChat");
            removed.put("nhom", mongo.remove(new Query(Criteria.where("_id").in(groupIds)), "Nhom").getDeletedCount());

            List<Object> rooms = mongo.findDistinct(new Query(Criteria.where("username").in(users)), "maPhongChat", "ThanhVienPhongChat", Object.class);
            mongo.remove(new Query(Criteria.where("maPhongChat").in(rooms)), "TinNhan");
            mongo.remove(new Query(Criteria.where("maPhongChat").in(rooms)), "ThanhVienPhongChat");
            removed.put("hoiThoai", mongo.remove(new Query(Criteria.where("_id").in(rooms)), "PhongChat").getDeletedCount());

            mongo.remove(new Query(new Criteria().orOperator(Criteria.where("nguoiGui").in(users), Criteria.where("nguoiNhan").in(users))), "KetBan");
            mongo.remove(new Query(Criteria.where("nguoiNhan").in(users)), "ThongBao");
            mongo.remove(new Query(Criteria.where("maNguoiDung").in(users)), "Favorite");
            mongo.remove(new Query(Criteria.where("maNguoiDung").in(users)), "ChiTietKhuyenMai");
        }
        mongo.remove(new Query(Criteria.where("_id").in(list(m, "vouchers"))), "KhuyenMai");
        mongo.remove(new Query(Criteria.where("_id").in(list(m, "images"))), "HinhAnhSan");
        removed.put("san", mongo.remove(new Query(Criteria.where("_id").in(list(m, "courts"))), "San").getDeletedCount());
        mongo.updateMulti(new Query(Criteria.where("_id").in(list(m, "pricedCourts"))), new Update().set("bangGia", List.of()), "San");
        mongo.updateMulti(new Query(Criteria.where("_id").in(list(m, "venueOwners"))), new Update().unset("ownerUsername"), "CumSan");
        mongo.updateMulti(new Query(Criteria.where("_id").in(list(m, "venueDescriptions"))), new Update().unset("moTa"), "CumSan");
        mongo.updateMulti(new Query(Criteria.where("_id").in(list(m, "venuePhones"))), new Update().unset("sDT"), "CumSan");
        removed.put("taiKhoan", users.isEmpty() ? 0 : mongo.remove(new Query(Criteria.where("_id").in(users)), "users").getDeletedCount());
        mongo.remove(new Query(Criteria.where("_id").is(MARKER_ID)), MARKER);
        return removed;
    }

    // ── Tiện ích ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static List<String> list(Document d, String key) {
        Object v = d.get(key);
        return v instanceof List<?> l ? (List<String>) l : List.of();
    }

    private void saveMarker(Track t) {
        Document d = new Document("_id", MARKER_ID)
                .append("ngayTao", LocalDateTime.now().toString())
                .append("users", t.users)
                .append("venueOwners", t.venueOwners)
                .append("venueDescriptions", t.venueDescriptions)
                .append("venuePhones", t.venuePhones)
                .append("images", t.images)
                .append("courts", t.courts)
                .append("pricedCourts", t.pricedCourts)
                .append("vouchers", t.vouchers)
                .append("bookings", t.bookings)
                .append("thongKe", new Document(t.stats));
        mongo.save(d, MARKER);
    }

    private static void safe(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException e) {
            log.debug("demo step skipped: {}", e.getMessage());
        }
    }

    private static final class Track {
        final List<String> users = new ArrayList<>();
        final List<String> venueOwners = new ArrayList<>();
        final List<String> venueDescriptions = new ArrayList<>();
        final List<String> venuePhones = new ArrayList<>();
        final List<String> images = new ArrayList<>();
        final List<String> courts = new ArrayList<>();
        final List<String> pricedCourts = new ArrayList<>();
        final List<String> vouchers = new ArrayList<>();
        final List<String> bookings = new ArrayList<>();
        final Map<String, PhieuDatSanEntity> upcoming = new HashMap<>();
        final Map<String, Object> stats = new LinkedHashMap<>();

        void count(String key) { count(key, 1); }

        void count(String key, int n) { stats.merge(key, n, (a, b) -> (Integer) a + (Integer) b); }
    }

}
