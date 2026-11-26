package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.ThongBaoEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.entity.nhom.NhomEntity;
import com.example.alopoapi.entity.nhom.ThamGiaTranEntity;
import com.example.alopoapi.entity.nhom.TranDauEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.NhomRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.ThamGiaTranRepo;
import com.example.alopoapi.repository.TranDauRepo;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.CHO;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.CHO_DUYET;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.CHO_XAC_NHAN;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.CHUA;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.DA_TRA;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.MIEN;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.THAM_GIA;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.TU_CHOI;
import static com.example.alopoapi.entity.nhom.ThamGiaTranEntity.VANG;
import static com.example.alopoapi.service.CommunityRules.bad;
import static com.example.alopoapi.service.CommunityRules.forbidden;
import static com.example.alopoapi.service.CommunityRules.get;
import static com.example.alopoapi.service.CommunityRules.notFound;

/**
 * Trận / kèo và ghép trận vãng lai.
 * <ul>
 *   <li>Chủ trận tạo trận từ một phiếu đặt sân của mình (chưa diễn ra, chưa huỷ).</li>
 *   <li>Trận của nhóm: thành viên nhóm bấm tham gia là vào (đủ người → danh sách chờ, tự lên khi có chỗ).</li>
 *   <li>Trận công khai: người ngoài xin vào, chủ trận duyệt.</li>
 *   <li>Chia tiền: mỗi người góp {@code chiPhiMoiNguoi}, gửi bill, chủ trận xác nhận. App chỉ ghi nhận, không giữ tiền.</li>
 * </ul>
 */
@Service
public class TranDauService {

    /** Trạng thái hiển thị (tính khi trả về). */
    static final String DU_NGUOI = "DU_NGUOI";
    static final String DANG_DIEN_RA = "DANG_DIEN_RA";
    static final String DA_KET_THUC = "DA_KET_THUC";

    private static final Set<String> ACTIVE_BOOKING = Set.of("CHO_XAC_NHAN", "DA_XAC_NHAN");
    private static final Set<String> IN_MATCH = Set.of(THAM_GIA, CHO, CHO_DUYET);

    private final TranDauRepo tranRepo;
    private final ThamGiaTranRepo thamGiaRepo;
    private final PhieuDatSanRepo phieuRepo;
    private final SanRepo sanRepo;
    private final CumSanRepo cumSanRepo;
    private final NhomRepo nhomRepo;
    private final UserRepo userRepo;
    private final NhomService nhomService;
    private final ManagementService managementService;

    public TranDauService(TranDauRepo tranRepo, ThamGiaTranRepo thamGiaRepo, PhieuDatSanRepo phieuRepo, SanRepo sanRepo,
                          CumSanRepo cumSanRepo, NhomRepo nhomRepo, UserRepo userRepo, NhomService nhomService,
                          ManagementService managementService) {
        this.tranRepo = tranRepo;
        this.thamGiaRepo = thamGiaRepo;
        this.phieuRepo = phieuRepo;
        this.sanRepo = sanRepo;
        this.cumSanRepo = cumSanRepo;
        this.nhomRepo = nhomRepo;
        this.userRepo = userRepo;
        this.nhomService = nhomService;
        this.managementService = managementService;
    }

    private ThongBaoService thongBao;

    @Autowired(required = false)
    void setThongBaoService(ThongBaoService thongBao) {
        this.thongBao = thongBao;
    }

    // ── Chủ trận ─────────────────────────────────────────────────────────────

    /** Phiếu đặt sắp tới của tôi chưa gắn trận nào (để chọn khi tạo trận). */
    public List<Map<String, Object>> eligibleBookings(String me) {
        LocalDateTime now = LocalDateTime.now();
        List<PhieuDatSanEntity> bookings = phieuRepo.findByMaNguoiDung(me).stream()
                .filter(b -> ACTIVE_BOOKING.contains(b.getTrangThai()) && b.getBatDau() != null && b.getBatDau().isAfter(now))
                .sorted(Comparator.comparing(PhieuDatSanEntity::getBatDau))
                .toList();
        if (bookings.isEmpty()) return List.of();
        Set<String> used = new HashSet<>();
        tranRepo.findByMaPhieuDatInAndTrangThai(bookings.stream().map(PhieuDatSanEntity::getMaPhieuDat).toList(), TranDauEntity.MO)
                .forEach(t -> used.add(t.getMaPhieuDat()));
        Lookup lookup = new Lookup();
        return bookings.stream().filter(b -> !used.contains(b.getMaPhieuDat())).map(b -> {
            SanEntity court = lookup.court(b.getMaSan());
            CumSanEntity venue = court == null ? null : lookup.venue(court.getMaCumSan());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("maPhieuDat", b.getMaPhieuDat());
            item.put("batDau", b.getBatDau());
            item.put("ketThuc", b.getKetThuc());
            item.put("tongTien", b.getTongTien());
            item.put("trangThai", b.getTrangThai());
            item.put("maSan", b.getMaSan());
            item.put("tenSan", court == null ? null : court.getTenSan());
            item.put("maCumSan", venue == null ? null : venue.getMaCumSan());
            item.put("tenCumSan", venue == null ? null : venue.getTenCumSan());
            item.put("diaChi", venue == null ? null : venue.getDiaChi());
            return item;
        }).toList();
    }

    public Map<String, Object> create(String me, Map<String, Object> body) {
        String bookingId = CommunityRules.text(get(body, "maPhieuDat"), 100, "Mã phiếu đặt");
        if (bookingId == null) throw bad("Hãy chọn lượt đặt sân cho trận");
        PhieuDatSanEntity booking = phieuRepo.findById(bookingId).orElseThrow(() -> notFound("Không tìm thấy phiếu đặt"));
        if (booking.getMaNguoiDung() == null || !booking.getMaNguoiDung().equalsIgnoreCase(me)) {
            throw forbidden("Bạn chỉ tạo trận được từ sân do chính mình đặt");
        }
        if (!ACTIVE_BOOKING.contains(booking.getTrangThai())) throw bad("Phiếu đặt đã huỷ hoặc đã hoàn thành");
        if (booking.getBatDau() == null || !booking.getBatDau().isAfter(LocalDateTime.now())) throw bad("Giờ chơi đã bắt đầu, không thể tạo trận");
        if (tranRepo.existsByMaPhieuDatAndTrangThai(bookingId, TranDauEntity.MO)) throw bad("Lượt đặt sân này đã có trận");

        String groupId = CommunityRules.text(get(body, "maNhom"), 40, "Nhóm");
        if (groupId != null) {
            nhomService.requireUsable(groupId);
            nhomService.requireMember(groupId, me);
        }
        SanEntity court = sanRepo.findById(booking.getMaSan()).orElseThrow(() -> notFound("Không tìm thấy sân"));

        LocalDateTime now = LocalDateTime.now();
        TranDauEntity match = new TranDauEntity();
        match.setMaTran("TR" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        match.setMaNhom(groupId);
        match.setMaPhieuDat(bookingId);
        match.setMaSan(court.getMaSan());
        match.setMaCumSan(court.getMaCumSan());
        match.setBatDau(booking.getBatDau());
        match.setKetThuc(booking.getKetThuc());
        match.setChuTran(me);
        match.setTrangThai(TranDauEntity.MO);
        match.setNgayTao(now);
        match.setKieuChoi("DOI");
        match.setSoNguoiToiDa(4);
        applyInfo(match, body, true, 1, booking.getTongTien());
        tranRepo.save(match);

        ThamGiaTranEntity host = participant(match, me, THAM_GIA);
        host.setSoTien(0);
        host.setThanhToan(MIEN);
        thamGiaRepo.save(host);

        if (groupId != null) {
            CumSanEntity venue = cumSanRepo.findById(court.getMaCumSan()).orElse(null);
            nhomService.systemMessage(groupId, displayName(me) + " vừa mở trận \"" + match.getTieuDe() + "\" lúc "
                    + when(match) + (venue == null ? "" : " tại " + venue.getTenCumSan()) + ". Vào tab Trận để điểm danh nhé!");
        }
        return detail(match.getMaTran(), me);
    }

    public Map<String, Object> update(String id, String me, Map<String, Object> body) {
        TranDauEntity match = requireHost(id, me);
        requireUpcoming(match);
        List<ThamGiaTranEntity> people = thamGiaRepo.findByMaTran(id);
        long going = people.stream().filter(ThamGiaTranEntity::isGoing).count();
        long oldFee = match.getChiPhiMoiNguoi();
        PhieuDatSanEntity booking = phieuRepo.findById(match.getMaPhieuDat()).orElse(null);
        applyInfo(match, body, false, (int) going, booking == null ? 0 : booking.getTongTien());
        tranRepo.save(match);

        if (oldFee != match.getChiPhiMoiNguoi()) {
            people.stream().filter(p -> CHUA.equals(p.getThanhToan())).forEach(p -> p.setSoTien(match.getChiPhiMoiNguoi()));
            thamGiaRepo.saveAll(people);
        }
        promoteWaitlist(match);
        return detail(id, me);
    }

    /** Chủ trận huỷ trận (không huỷ phiếu đặt sân). */
    public Map<String, Object> cancel(String id, String me) {
        TranDauEntity match = requireHost(id, me);
        requireUpcoming(match);
        match.setTrangThai(TranDauEntity.DA_HUY);
        tranRepo.save(match);
        nhomService.systemMessage(match.getMaNhom(), displayName(me) + " đã huỷ trận \"" + match.getTieuDe() + "\" lúc " + when(match));
        if (thongBao != null) thongBao.matchCancelled(match, displayName(me) + " đã huỷ trận lúc " + when(match) + ".");
        return detail(id, me);
    }

    /** Duyệt / từ chối người xin vào. */
    public Map<String, Object> review(String id, String me, String target, boolean accept) {
        TranDauEntity match = requireHost(id, me);
        requireUpcoming(match);
        ThamGiaTranEntity request = thamGiaRepo.findById(ThamGiaTranEntity.idOf(id, canonical(target)))
                .filter(p -> CHO_DUYET.equals(p.getTrangThai()) || CHO.equals(p.getTrangThai()))
                .orElseThrow(() -> notFound("Không tìm thấy yêu cầu tham gia"));
        if (accept) {
            if (goingCount(id) >= match.getSoNguoiToiDa()) throw bad("Trận đã đủ người. Hãy tăng số người tối đa trước khi duyệt thêm");
            request.setTrangThai(THAM_GIA);
        } else {
            request.setTrangThai(TU_CHOI);
        }
        request.setNgayCapNhat(LocalDateTime.now());
        thamGiaRepo.save(request);
        if (thongBao != null) {
            if (accept) {
                thongBao.matchEvent(match, request.getUsername(), ThongBaoEntity.TRAN_DUOC_DUYET, "Bạn đã được nhận vào trận",
                        "\"" + match.getTieuDe() + "\" lúc " + when(match) + ". Nhớ góp tiền sân cho chủ trận nhé!");
            } else {
                thongBao.matchEvent(match, request.getUsername(), ThongBaoEntity.TRAN_TU_CHOI, "Yêu cầu vào trận chưa được nhận",
                        "Chủ trận \"" + match.getTieuDe() + "\" chưa nhận thêm người. Thử tìm trận khác nhé.");
            }
        }
        return detail(id, me);
    }

    /** Mời một người ra khỏi trận. */
    public Map<String, Object> kick(String id, String me, String target) {
        TranDauEntity match = requireHost(id, me);
        requireUpcoming(match);
        ThamGiaTranEntity p = thamGiaRepo.findById(ThamGiaTranEntity.idOf(id, canonical(target)))
                .filter(x -> IN_MATCH.contains(x.getTrangThai()))
                .orElseThrow(() -> notFound("Người này không có trong trận"));
        if (p.getUsername().equalsIgnoreCase(match.getChuTran())) throw bad("Không thể mời chủ trận ra");
        p.setTrangThai(TU_CHOI);
        p.setNgayCapNhat(LocalDateTime.now());
        thamGiaRepo.save(p);
        promoteWaitlist(match);
        return detail(id, me);
    }

    /** Chủ trận xác nhận đã nhận tiền (hoặc đánh dấu lại là chưa trả). */
    public Map<String, Object> confirmPayment(String id, String me, String target, boolean paid) {
        requireHost(id, me);
        ThamGiaTranEntity p = thamGiaRepo.findById(ThamGiaTranEntity.idOf(id, canonical(target)))
                .filter(x -> !MIEN.equals(x.getThanhToan()))
                .orElseThrow(() -> notFound("Không tìm thấy người này trong trận"));
        p.setThanhToan(paid ? DA_TRA : CHUA);
        p.setNgayCapNhat(LocalDateTime.now());
        thamGiaRepo.save(p);
        return detail(id, me);
    }

    // ── Người chơi ───────────────────────────────────────────────────────────

    /**
     * Tham gia: thành viên nhóm vào thẳng (đủ người → danh sách chờ);
     * người ngoài xin vào trận công khai và chờ chủ trận duyệt.
     */
    public Map<String, Object> join(String id, String me, Map<String, Object> body) {
        TranDauEntity match = requireMatch(id);
        requireUpcoming(match);
        ThamGiaTranEntity existing = thamGiaRepo.findById(ThamGiaTranEntity.idOf(id, me)).orElse(null);
        if (existing != null && IN_MATCH.contains(existing.getTrangThai())) return detail(id, me);
        if (existing != null && TU_CHOI.equals(existing.getTrangThai())) throw forbidden("Chủ trận đã từ chối yêu cầu của bạn");

        boolean groupMember = nhomService.isMember(match.getMaNhom(), me);
        boolean full = goingCount(id) >= match.getSoNguoiToiDa();
        String status;
        if (groupMember) {
            status = full ? CHO : THAM_GIA;
        } else {
            if (!match.isCongKhai()) throw forbidden("Trận này chỉ dành cho thành viên nhóm");
            if (full) throw bad("Trận đã đủ người");
            status = CHO_DUYET;
        }
        ThamGiaTranEntity p = existing != null ? existing : participant(match, me, status);
        p.setTrangThai(status);
        p.setSoTien(match.getChiPhiMoiNguoi());
        if (p.getThanhToan() == null || MIEN.equals(p.getThanhToan())) p.setThanhToan(CHUA);
        p.setLoiNhan(CommunityRules.text(get(body, "loiNhan"), 300, "Lời nhắn"));
        p.setTrinhDo(CommunityRules.level(get(body, "trinhDo")));
        p.setNgayThamGia(LocalDateTime.now());
        p.setNgayCapNhat(LocalDateTime.now());
        thamGiaRepo.save(p);
        if (thongBao != null && CHO_DUYET.equals(status)) {
            thongBao.matchEvent(match, match.getChuTran(), ThongBaoEntity.TRAN_XIN_VAO, displayName(me) + " xin vào trận",
                    "\"" + match.getTieuDe() + "\" lúc " + when(match) + (p.getLoiNhan() == null ? "" : ". Lời nhắn: " + p.getLoiNhan()));
        }
        return detail(id, me);
    }

    /** Rời trận / báo vắng (thành viên nhóm có thể báo vắng trước khi điểm danh). */
    public Map<String, Object> leave(String id, String me) {
        TranDauEntity match = requireMatch(id);
        requireUpcoming(match);
        if (match.getChuTran().equalsIgnoreCase(me)) throw bad("Chủ trận không thể rời trận. Hãy huỷ trận nếu không chơi nữa");
        ThamGiaTranEntity p = thamGiaRepo.findById(ThamGiaTranEntity.idOf(id, me)).orElse(null);
        if (p == null) {
            if (!nhomService.isMember(match.getMaNhom(), me)) throw notFound("Bạn chưa tham gia trận này");
            p = participant(match, me, VANG);
            p.setThanhToan(CHUA);
        }
        if (TU_CHOI.equals(p.getTrangThai())) return detail(id, me);
        boolean wasGoing = p.isGoing();
        p.setTrangThai(VANG);
        p.setNgayCapNhat(LocalDateTime.now());
        thamGiaRepo.save(p);
        if (wasGoing) promoteWaitlist(match);
        return detail(id, me);
    }

    /** Báo đã chuyển tiền (kèm ảnh bill nếu có). */
    public Map<String, Object> pay(String id, String me, Map<String, Object> body) {
        requireMatch(id);
        ThamGiaTranEntity p = thamGiaRepo.findById(ThamGiaTranEntity.idOf(id, me))
                .filter(ThamGiaTranEntity::isGoing)
                .orElseThrow(() -> forbidden("Bạn chưa ở trong danh sách chơi của trận này"));
        if (MIEN.equals(p.getThanhToan())) throw bad("Chủ trận không cần góp tiền");
        if (DA_TRA.equals(p.getThanhToan())) throw bad("Chủ trận đã xác nhận bạn trả tiền rồi");
        String bill = CommunityRules.billUrl(get(body, "billUrl"));
        if (bill != null) p.setBillUrl(bill);
        p.setThanhToan(CHO_XAC_NHAN);
        p.setNgayCapNhat(LocalDateTime.now());
        thamGiaRepo.save(p);
        if (thongBao != null) {
            TranDauEntity match = requireMatch(id);
            thongBao.matchEvent(match, match.getChuTran(), ThongBaoEntity.TRAN_DA_CHUYEN_TIEN, displayName(me) + " báo đã chuyển tiền",
                    "Trận \"" + match.getTieuDe() + "\": kiểm tra tài khoản rồi bấm Đã nhận nhé.");
        }
        return detail(id, me);
    }

    // ── Danh sách ────────────────────────────────────────────────────────────

    /** Tìm trận vãng lai: trận công khai sắp diễn ra, mới nhất theo giờ chơi. Không cần đăng nhập. */
    public List<Map<String, Object>> search(String venueId, String date, String level, String type, boolean onlyAvailable, String me) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate day = parseDate(date);
        String lv = CommunityRules.level(level);
        String kind = type == null || type.isBlank() ? null : type.trim().toUpperCase();
        List<TranDauEntity> matches = tranRepo.findByCongKhaiTrueAndTrangThaiAndBatDauAfter(TranDauEntity.MO, now).stream()
                .filter(m -> venueId == null || venueId.isBlank() || venueId.equals(m.getMaCumSan()))
                .filter(m -> day == null || m.getBatDau().toLocalDate().equals(day))
                .filter(m -> kind == null || kind.equals(m.getKieuChoi()))
                .filter(m -> lv == null || fitsLevel(m, lv))
                .sorted(Comparator.comparing(TranDauEntity::getBatDau))
                .limit(150)
                .toList();
        List<Map<String, Object>> result = summaries(matches, me, false);
        return result.stream()
                .filter(m -> !TranDauEntity.DA_HUY.equals(m.get("trangThai")))
                .filter(m -> !onlyAvailable || TranDauEntity.MO.equals(m.get("trangThai")))
                .toList();
    }

    /** Trận tôi tổ chức hoặc tham gia (kể cả đang chờ duyệt, bị từ chối). Sắp tới trước, rồi đã qua. */
    public List<Map<String, Object>> mine(String me) {
        List<ThamGiaTranEntity> mine = thamGiaRepo.findByUsername(me).stream()
                .filter(p -> !VANG.equals(p.getTrangThai()))
                .toList();
        if (mine.isEmpty()) return List.of();
        List<TranDauEntity> matches = new ArrayList<>();
        tranRepo.findAllById(mine.stream().map(ThamGiaTranEntity::getMaTran).toList()).forEach(matches::add);
        return ordered(summaries(matches, me, false));
    }

    /** Trận của nhóm (chỉ thành viên). */
    public List<Map<String, Object>> groupMatches(String groupId, String me) {
        nhomService.requireGroup(groupId);
        nhomService.requireMember(groupId, me);
        return ordered(summaries(tranRepo.findByMaNhom(groupId), me, false));
    }

    /**
     * Chi tiết trận. Xem được nếu: trận công khai, là thành viên nhóm của trận, hoặc đang có tên trong trận.
     * Chủ trận thấy thêm người xin vào, lời nhắn, bill; người trong trận thấy thông tin nhận tiền.
     */
    public Map<String, Object> detail(String id, String me) {
        TranDauEntity match = requireMatch(id);
        List<ThamGiaTranEntity> people = thamGiaRepo.findByMaTran(id);
        ThamGiaTranEntity mine = people.stream().filter(p -> p.getUsername().equalsIgnoreCase(me)).findFirst().orElse(null);
        boolean host = match.getChuTran().equalsIgnoreCase(me);
        boolean groupMember = nhomService.isMember(match.getMaNhom(), me);
        if (!match.isCongKhai() && !groupMember && mine == null && !host) throw forbidden("Trận này chỉ dành cho thành viên nhóm");

        Lookup lookup = new Lookup();
        Map<String, Object> result = summary(match, people, mine, lookup, groupMember);
        result.put("ghiChu", match.getGhiChu());
        boolean going = mine != null && mine.isGoing();
        if (host || going) {
            Map<String, Object> bank = new LinkedHashMap<>();
            bank.put("nganHang", match.getNganHang());
            bank.put("soTaiKhoan", match.getSoTaiKhoan());
            bank.put("chuTaiKhoan", match.getChuTaiKhoan());
            result.put("nhanTien", match.getSoTaiKhoan() == null ? null : bank);
        }
        Set<String> visible = host ? Set.of(THAM_GIA, CHO, CHO_DUYET, VANG, TU_CHOI)
                : groupMember ? Set.of(THAM_GIA, CHO, VANG) : Set.of(THAM_GIA, CHO);
        Map<String, Integer> played = new HashMap<>();
        result.put("thanhVien", people.stream()
                .filter(p -> visible.contains(p.getTrangThai()))
                .sorted(Comparator.comparing((ThamGiaTranEntity p) -> personOrder(p, match))
                        .thenComparing(ThamGiaTranEntity::getNgayThamGia, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(p -> {
                    boolean self = p.getUsername().equalsIgnoreCase(me);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("username", p.getUsername());
                    item.put("ten", CommunityRules.fullName(lookup.user(p.getUsername()), p.getUsername()));
                    item.put("trangThai", p.getTrangThai());
                    item.put("laChuTran", p.getUsername().equalsIgnoreCase(match.getChuTran()));
                    item.put("trinhDo", p.getTrinhDo());
                    if (host || self) {
                        item.put("thanhToan", p.getThanhToan());
                        item.put("soTien", p.getSoTien());
                        item.put("billUrl", p.getBillUrl());
                    } else {
                        item.put("thanhToan", DA_TRA.equals(p.getThanhToan()) || MIEN.equals(p.getThanhToan()) ? p.getThanhToan() : null);
                    }
                    if (host) {
                        item.put("loiNhan", p.getLoiNhan());
                        if (CHO_DUYET.equals(p.getTrangThai())) {
                            item.put("soTranDaChoi", played.computeIfAbsent(p.getUsername(), this::playedCount));
                        }
                    }
                    return item;
                }).toList());
        if (host) {
            long paid = people.stream().filter(p -> DA_TRA.equals(p.getThanhToan())).mapToLong(ThamGiaTranEntity::getSoTien).sum();
            long expected = people.stream().filter(p -> p.isGoing() && !MIEN.equals(p.getThanhToan())).mapToLong(ThamGiaTranEntity::getSoTien).sum();
            result.put("daThu", paid);
            result.put("canThu", expected);
            PhieuDatSanEntity booking = lookup.booking(match.getMaPhieuDat());
            result.put("tienSan", booking == null ? null : booking.getTongTien());
        }
        return result;
    }

    // ── Quản lý (chủ sân / admin) ────────────────────────────────────────────

    public List<Map<String, Object>> managed(String venueId, Authentication authentication) {
        List<String> venueIds = managementService.venues(authentication).stream()
                .map(CumSanEntity::getMaCumSan)
                .filter(v -> venueId == null || venueId.isBlank() || v.equals(venueId))
                .toList();
        if (venueIds.isEmpty()) return List.of();
        List<TranDauEntity> matches = tranRepo.findByMaCumSanIn(venueIds);
        List<Map<String, Object>> result = new ArrayList<>(summaries(matches, null, true));
        result.sort(Comparator.comparing((Map<String, Object> m) -> (LocalDateTime) m.get("batDau")).reversed());
        return result;
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private void applyInfo(TranDauEntity match, Map<String, Object> body, boolean creating, int minPlayers, double courtPrice) {
        if (body == null) body = Map.of();
        if (creating || body.containsKey("kieuChoi")) {
            String type = CommunityRules.text(get(body, "kieuChoi"), 20, "Kiểu chơi");
            type = type == null ? "DOI" : type.toUpperCase();
            if (!CommunityRules.PLAY_TYPES.contains(type)) throw bad("Kiểu chơi không hợp lệ");
            match.setKieuChoi(type);
        }
        if (creating || body.containsKey("soNguoiToiDa")) {
            Integer max = CommunityRules.integer(get(body, "soNguoiToiDa"));
            if (max == null) max = switch (match.getKieuChoi()) { case "DON" -> 2; case "GIAO_LUU" -> 8; default -> 4; };
            if (max < 2 || max > 24) throw bad("Số người từ 2 đến 24");
            if (max < minPlayers) throw bad("Trận đang có " + minPlayers + " người tham gia, không thể giảm xuống " + max);
            match.setSoNguoiToiDa(max);
        }
        if (creating || body.containsKey("tieuDe")) {
            String title = CommunityRules.text(get(body, "tieuDe"), 80, "Tiêu đề");
            match.setTieuDe(title != null ? title : switch (match.getKieuChoi()) {
                case "DON" -> "Tìm bạn đánh đơn";
                case "GIAO_LUU" -> "Giao lưu cầu lông";
                default -> "Kèo đánh đôi";
            });
        }
        if (creating || body.containsKey("ghiChu")) match.setGhiChu(CommunityRules.text(get(body, "ghiChu"), 500, "Ghi chú"));
        if (creating || body.containsKey("trinhDoTu") || body.containsKey("trinhDoDen")) {
            String from = CommunityRules.level(get(body, "trinhDoTu"));
            String to = CommunityRules.level(get(body, "trinhDoDen"));
            if (from != null && to != null && CommunityRules.levelIndex(from) > CommunityRules.levelIndex(to)) throw bad("Khoảng trình độ không hợp lệ");
            match.setTrinhDoTu(from);
            match.setTrinhDoDen(to);
        }
        if (creating || body.containsKey("chiPhiMoiNguoi")) {
            Long fee = CommunityRules.number(get(body, "chiPhiMoiNguoi"));
            if (fee == null) fee = suggestedFee(courtPrice, match.getSoNguoiToiDa());
            if (fee < 0 || fee > 5_000_000) throw bad("Chi phí mỗi người không hợp lệ");
            match.setChiPhiMoiNguoi(fee);
        }
        if (match.getMaNhom() == null) {
            match.setCongKhai(true);
        } else if (creating || body.containsKey("congKhai")) {
            match.setCongKhai(CommunityRules.bool(get(body, "congKhai"), false));
        }
        if (creating || body.containsKey("soTaiKhoan") || body.containsKey("nganHang")) {
            String account = CommunityRules.text(get(body, "soTaiKhoan"), 30, "Số tài khoản");
            if (account == null) {
                match.setNganHang(null);
                match.setSoTaiKhoan(null);
                match.setChuTaiKhoan(null);
            } else {
                account = account.replaceAll("\\s", "");
                if (!account.matches("\\d{6,20}")) throw bad("Số tài khoản chỉ gồm 6–20 chữ số");
                String bank = CommunityRules.text(get(body, "nganHang"), 10, "Ngân hàng");
                bank = bank == null ? null : bank.toUpperCase();
                if (bank == null || !CommunityRules.BANKS.contains(bank)) throw bad("Hãy chọn ngân hàng nhận tiền");
                match.setNganHang(bank);
                match.setSoTaiKhoan(account);
                match.setChuTaiKhoan(CommunityRules.text(get(body, "chuTaiKhoan"), 60, "Tên chủ tài khoản"));
            }
        }
    }

    /** Tiền sân chia đều, làm tròn lên nghìn đồng. */
    static long suggestedFee(double courtPrice, int players) {
        if (courtPrice <= 0 || players <= 0) return 0;
        return (long) Math.ceil(courtPrice / players / 1000.0) * 1000;
    }

    static boolean fitsLevel(TranDauEntity m, String level) {
        int idx = CommunityRules.levelIndex(level);
        int from = CommunityRules.levelIndex(m.getTrinhDoTu());
        int to = CommunityRules.levelIndex(m.getTrinhDoDen());
        return (from < 0 || idx >= from) && (to < 0 || idx <= to);
    }

    /** Thành viên nhóm trong danh sách chờ tự lên khi có chỗ trống. */
    private void promoteWaitlist(TranDauEntity match) {
        if (!TranDauEntity.MO.equals(match.getTrangThai())) return;
        List<ThamGiaTranEntity> people = thamGiaRepo.findByMaTran(match.getMaTran());
        long going = people.stream().filter(ThamGiaTranEntity::isGoing).count();
        List<ThamGiaTranEntity> waiting = people.stream().filter(p -> CHO.equals(p.getTrangThai()))
                .sorted(Comparator.comparing(ThamGiaTranEntity::getNgayThamGia, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<ThamGiaTranEntity> promoted = new ArrayList<>();
        for (ThamGiaTranEntity p : waiting) {
            if (going >= match.getSoNguoiToiDa()) break;
            p.setTrangThai(THAM_GIA);
            p.setNgayCapNhat(LocalDateTime.now());
            promoted.add(p);
            going++;
        }
        if (!promoted.isEmpty()) {
            thamGiaRepo.saveAll(promoted);
            if (thongBao != null) {
                promoted.forEach(p -> thongBao.matchEvent(match, p.getUsername(), ThongBaoEntity.TRAN_LEN_DANH_SACH,
                        "Bạn đã có chỗ trong trận", "Có người rời trận nên bạn được lên danh sách chơi \"" + match.getTieuDe() + "\" lúc " + when(match) + "."));
            }
        }
    }

    private long goingCount(String matchId) {
        return thamGiaRepo.findByMaTran(matchId).stream().filter(ThamGiaTranEntity::isGoing).count();
    }

    /** Số trận đã chơi xong (để chủ trận tham khảo khi duyệt người lạ). */
    private int playedCount(String username) {
        List<String> ids = thamGiaRepo.findByUsernameAndTrangThai(username, THAM_GIA).stream().map(ThamGiaTranEntity::getMaTran).toList();
        if (ids.isEmpty()) return 0;
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (TranDauEntity m : tranRepo.findAllById(ids)) {
            if (TranDauEntity.MO.equals(m.getTrangThai()) && m.getKetThuc() != null && m.getKetThuc().isBefore(now)) count++;
        }
        return count;
    }

    private TranDauEntity requireMatch(String id) {
        return tranRepo.findById(id == null ? "" : id).orElseThrow(() -> notFound("Không tìm thấy trận"));
    }

    private TranDauEntity requireHost(String id, String me) {
        TranDauEntity match = requireMatch(id);
        if (!match.getChuTran().equalsIgnoreCase(me)) throw forbidden("Chỉ chủ trận được làm việc này");
        return match;
    }

    private void requireUpcoming(TranDauEntity match) {
        if (TranDauEntity.DA_HUY.equals(match.getTrangThai())) throw bad("Trận đã bị huỷ");
        if (match.getBatDau() == null || !match.getBatDau().isAfter(LocalDateTime.now())) throw bad("Trận đã bắt đầu hoặc đã kết thúc");
        phieuRepo.findById(match.getMaPhieuDat()).filter(b -> "DA_HUY".equals(b.getTrangThai()))
                .ifPresent(b -> { throw bad("Lượt đặt sân của trận đã bị huỷ"); });
    }

    private ThamGiaTranEntity participant(TranDauEntity match, String username, String status) {
        ThamGiaTranEntity p = new ThamGiaTranEntity();
        p.setId(ThamGiaTranEntity.idOf(match.getMaTran(), username));
        p.setMaTran(match.getMaTran());
        p.setUsername(username);
        p.setTrangThai(status);
        p.setNgayThamGia(LocalDateTime.now());
        p.setNgayCapNhat(LocalDateTime.now());
        return p;
    }

    private List<Map<String, Object>> summaries(List<TranDauEntity> matches, String me, boolean management) {
        if (matches.isEmpty()) return List.of();
        Map<String, List<ThamGiaTranEntity>> people = new HashMap<>();
        thamGiaRepo.findByMaTranIn(matches.stream().map(TranDauEntity::getMaTran).toList())
                .forEach(p -> people.computeIfAbsent(p.getMaTran(), k -> new ArrayList<>()).add(p));
        Lookup lookup = new Lookup();
        return matches.stream().map(m -> {
            List<ThamGiaTranEntity> list = people.getOrDefault(m.getMaTran(), List.of());
            ThamGiaTranEntity mine = me == null ? null : list.stream().filter(p -> p.getUsername().equalsIgnoreCase(me)).findFirst().orElse(null);
            Map<String, Object> item = summary(m, list, mine, lookup, me != null && nhomService.isMember(m.getMaNhom(), me));
            if (management) {
                item.put("nguoiChoi", list.stream().filter(ThamGiaTranEntity::isGoing)
                        .map(p -> CommunityRules.fullName(lookup.user(p.getUsername()), p.getUsername())).toList());
            }
            return item;
        }).toList();
    }

    private Map<String, Object> summary(TranDauEntity m, List<ThamGiaTranEntity> people, ThamGiaTranEntity mine, Lookup lookup, boolean groupMember) {
        long going = people.stream().filter(ThamGiaTranEntity::isGoing).count();
        CumSanEntity venue = lookup.venue(m.getMaCumSan());
        SanEntity court = lookup.court(m.getMaSan());
        NhomEntity group = lookup.group(m.getMaNhom());
        PhieuDatSanEntity booking = lookup.booking(m.getMaPhieuDat());

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("maTran", m.getMaTran());
        item.put("tieuDe", m.getTieuDe());
        item.put("kieuChoi", m.getKieuChoi());
        item.put("maNhom", group == null ? null : group.getMaNhom());
        item.put("tenNhom", group == null ? null : group.getTenNhom());
        item.put("mauNhom", group == null ? null : group.getMau());
        item.put("maPhieuDat", m.getMaPhieuDat());
        item.put("maCumSan", m.getMaCumSan());
        item.put("tenCumSan", venue == null ? null : venue.getTenCumSan());
        item.put("diaChi", venue == null ? null : venue.getDiaChi());
        item.put("maSan", m.getMaSan());
        item.put("tenSan", court == null ? null : court.getTenSan());
        item.put("batDau", m.getBatDau());
        item.put("ketThuc", m.getKetThuc());
        Map<String, Object> host = new LinkedHashMap<>();
        host.put("username", m.getChuTran());
        host.put("ten", CommunityRules.fullName(lookup.user(m.getChuTran()), m.getChuTran()));
        item.put("chuTran", host);
        item.put("soNguoiToiDa", m.getSoNguoiToiDa());
        item.put("soDaThamGia", going);
        item.put("soCho", people.stream().filter(p -> CHO.equals(p.getTrangThai())).count());
        item.put("soChoDuyet", people.stream().filter(p -> CHO_DUYET.equals(p.getTrangThai())).count());
        item.put("trinhDoTu", m.getTrinhDoTu());
        item.put("trinhDoDen", m.getTrinhDoDen());
        item.put("chiPhiMoiNguoi", m.getChiPhiMoiNguoi());
        item.put("congKhai", m.isCongKhai());
        item.put("trangThai", displayStatus(m, booking, going));
        item.put("ngayTao", m.getNgayTao());
        item.put("nguoiChoiTieuBieu", people.stream().filter(ThamGiaTranEntity::isGoing)
                .sorted(Comparator.comparing((ThamGiaTranEntity p) -> personOrder(p, m)))
                .limit(6)
                .map(p -> CommunityRules.fullName(lookup.user(p.getUsername()), p.getUsername()))
                .toList());
        if (mine != null || groupMember) {
            Map<String, Object> me = new LinkedHashMap<>();
            me.put("trangThai", mine == null ? null : mine.getTrangThai());
            me.put("thanhToan", mine == null ? null : mine.getThanhToan());
            me.put("soTien", mine == null ? m.getChiPhiMoiNguoi() : mine.getSoTien());
            me.put("laChuTran", mine != null && m.getChuTran().equalsIgnoreCase(mine.getUsername()));
            me.put("laThanhVienNhom", groupMember);
            item.put("toi", me);
        } else {
            item.put("toi", null);
        }
        return item;
    }

    static String displayStatus(TranDauEntity m, PhieuDatSanEntity booking, long going) {
        LocalDateTime now = LocalDateTime.now();
        if (TranDauEntity.DA_HUY.equals(m.getTrangThai()) || (booking != null && "DA_HUY".equals(booking.getTrangThai()))) return TranDauEntity.DA_HUY;
        if (m.getKetThuc() != null && m.getKetThuc().isBefore(now)) return DA_KET_THUC;
        if (m.getBatDau() != null && !m.getBatDau().isAfter(now)) return DANG_DIEN_RA;
        if (going >= m.getSoNguoiToiDa()) return DU_NGUOI;
        return TranDauEntity.MO;
    }

    /** Sắp tới (gần nhất trước) rồi đến các trận đã qua / đã huỷ (mới nhất trước). */
    private static List<Map<String, Object>> ordered(List<Map<String, Object>> list) {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> upcoming = new ArrayList<>();
        List<Map<String, Object>> past = new ArrayList<>();
        for (Map<String, Object> m : list) {
            LocalDateTime end = (LocalDateTime) m.get("ketThuc");
            boolean done = TranDauEntity.DA_HUY.equals(m.get("trangThai")) || (end != null && end.isBefore(now));
            (done ? past : upcoming).add(m);
        }
        upcoming.sort(Comparator.comparing((Map<String, Object> m) -> (LocalDateTime) m.get("batDau")));
        past.sort(Comparator.comparing((Map<String, Object> m) -> (LocalDateTime) m.get("batDau")).reversed());
        upcoming.addAll(past);
        return upcoming;
    }

    private static int personOrder(ThamGiaTranEntity p, TranDauEntity m) {
        if (p.getUsername().equalsIgnoreCase(m.getChuTran())) return 0;
        return switch (p.getTrangThai()) {
            case THAM_GIA -> 1;
            case CHO -> 2;
            case CHO_DUYET -> 3;
            case VANG -> 4;
            default -> 5;
        };
    }

    private static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception e) {
            throw bad("Ngày không hợp lệ (yyyy-MM-dd)");
        }
    }

    private static String when(TranDauEntity m) {
        LocalDateTime t = m.getBatDau();
        return String.format("%02d:%02d ngày %02d/%02d", t.getHour(), t.getMinute(), t.getDayOfMonth(), t.getMonthValue());
    }

    private String displayName(String username) {
        return CommunityRules.fullName(userRepo.findByUsernameIgnoreCase(username).orElse(null), username);
    }

    private String canonical(String username) {
        if (username == null) throw bad("Thiếu tài khoản");
        return userRepo.findByUsernameIgnoreCase(username.trim()).map(UserEntity::getUsername).orElse(username.trim());
    }

    /** Cache tra cứu trong một request. */
    private final class Lookup {
        private final Map<String, Optional<UserEntity>> users = new HashMap<>();
        private final Map<String, Optional<SanEntity>> courts = new HashMap<>();
        private final Map<String, Optional<CumSanEntity>> venues = new HashMap<>();
        private final Map<String, Optional<NhomEntity>> groups = new HashMap<>();
        private final Map<String, Optional<PhieuDatSanEntity>> bookings = new HashMap<>();

        UserEntity user(String username) {
            return username == null ? null : users.computeIfAbsent(username, userRepo::findByUsernameIgnoreCase).orElse(null);
        }

        SanEntity court(String id) {
            return id == null ? null : courts.computeIfAbsent(id, sanRepo::findById).orElse(null);
        }

        CumSanEntity venue(String id) {
            return id == null ? null : venues.computeIfAbsent(id, cumSanRepo::findById).orElse(null);
        }

        NhomEntity group(String id) {
            return id == null ? null : groups.computeIfAbsent(id, nhomRepo::findById).orElse(null);
        }

        PhieuDatSanEntity booking(String id) {
            return id == null ? null : bookings.computeIfAbsent(id, phieuRepo::findById).orElse(null);
        }
    }
}
