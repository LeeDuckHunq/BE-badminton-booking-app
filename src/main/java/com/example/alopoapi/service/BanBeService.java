package com.example.alopoapi.service;

import com.example.alopoapi.entity.KetBanEntity;
import com.example.alopoapi.entity.ThongBaoEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.entity.chat.ThanhVienPhongChatEntity;
import com.example.alopoapi.entity.nhom.ThamGiaTranEntity;
import com.example.alopoapi.entity.nhom.ThanhVienNhomEntity;
import com.example.alopoapi.repository.KetBanRepo;
import com.example.alopoapi.repository.ThamGiaTranRepo;
import com.example.alopoapi.repository.ThanhVienNhomRepo;
import com.example.alopoapi.repository.ThanhVienPhongChatRepo;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bạn bè và quyền nhắn tin.
 * <ul>
 *   <li>Không còn danh bạ toàn bộ người dùng: muốn nhắn với người lạ phải tìm (đúng tên đăng nhập / số điện thoại
 *       hoặc một phần tên ≥ 3 ký tự) rồi gửi lời mời kết bạn.</li>
 *   <li>Được mở cuộc trò chuyện mới khi: là bạn bè, một bên là chủ sân / quản trị (khách hỏi chủ sân và ngược lại),
 *       hoặc cùng nhóm / cùng trận ghép. Cuộc trò chuyện đã có thì vẫn nhắn tiếp được.</li>
 * </ul>
 */
@Service
public class BanBeService {

    static final int MAX_FRIENDS = 500;
    private static final int SEARCH_LIMIT = 10;

    private final KetBanRepo repo;
    private final UserRepo userRepo;
    private final ThanhVienPhongChatRepo roomMemberRepo;
    private final ThanhVienNhomRepo groupMemberRepo;
    private final ThamGiaTranRepo matchPlayerRepo;
    private ThongBaoService thongBao;

    public BanBeService(KetBanRepo repo, UserRepo userRepo, ThanhVienPhongChatRepo roomMemberRepo,
                        ThanhVienNhomRepo groupMemberRepo, ThamGiaTranRepo matchPlayerRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.roomMemberRepo = roomMemberRepo;
        this.groupMemberRepo = groupMemberRepo;
        this.matchPlayerRepo = matchPlayerRepo;
    }

    @Autowired(required = false)
    void setThongBaoService(ThongBaoService thongBao) {
        this.thongBao = thongBao;
    }

    // ── Danh sách ───────────────────────────────────────────────────────────

    /** {banBe, loiMoiDen, loiMoiDi}: mỗi phần tử là hồ sơ ngắn của người kia + thời gian. */
    public Map<String, Object> overview(String me) {
        List<Map<String, Object>> friends = new ArrayList<>(), incoming = new ArrayList<>(), outgoing = new ArrayList<>();
        for (KetBanEntity k : relations(me)) {
            UserEntity other = userRepo.findByUsernameIgnoreCase(k.other(me)).filter(UserEntity::isActive).orElse(null);
            if (other == null) continue;
            Map<String, Object> item = profile(other);
            item.put("quanHe", relationOf(k, me));
            if (k.isFriend()) {
                item.put("tuNgay", k.getNgayKetBan());
                friends.add(item);
            } else {
                item.put("ngayGui", k.getNgayTao());
                (k.getNguoiNhan().equalsIgnoreCase(me) ? incoming : outgoing).add(item);
            }
        }
        friends.sort(Comparator.comparing(m -> String.valueOf(m.get("fullName")).toLowerCase(Locale.ROOT)));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("banBe", friends);
        result.put("loiMoiDen", incoming);
        result.put("loiMoiDi", outgoing);
        return result;
    }

    /**
     * Tìm người để kết bạn: đúng số điện thoại (≥ 9 số), hoặc tên đăng nhập / họ tên chứa chuỗi tìm (≥ 3 ký tự).
     * Không trả về số điện thoại, email; không liệt kê quản trị viên.
     */
    public List<Map<String, Object>> search(String me, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String digits = q.replaceAll("\\D", "");
        boolean byPhone = digits.length() >= 9 && digits.length() == q.replaceAll("[\\s.+]", "").length();
        if (!byPhone && q.length() < 3) return List.of();
        Map<String, KetBanEntity> rel = relations(me).stream().collect(Collectors.toMap(k -> k.other(me).toLowerCase(Locale.ROOT), k -> k, (a, b) -> a));
        String tail = digits.length() > 9 ? digits.substring(digits.length() - 9) : digits;
        return userRepo.findAll().stream()
                .filter(UserEntity::isActive)
                .filter(u -> !u.getUsername().equalsIgnoreCase(me))
                .filter(u -> !"ROLE_ADMIN".equals(role(u)))
                .filter(u -> byPhone
                        ? u.getPhoneNumber() != null && u.getPhoneNumber().replaceAll("\\D", "").endsWith(tail)
                        : u.getUsername().toLowerCase(Locale.ROOT).contains(q)
                          || (u.getFullName() != null && u.getFullName().toLowerCase(Locale.ROOT).contains(q)))
                .sorted(Comparator.comparing((UserEntity u) -> !u.getUsername().equalsIgnoreCase(q)).thenComparing(UserEntity::getUsername))
                .limit(SEARCH_LIMIT)
                .map(u -> {
                    Map<String, Object> item = profile(u);
                    item.put("quanHe", relationOf(rel.get(u.getUsername().toLowerCase(Locale.ROOT)), me));
                    return item;
                })
                .toList();
    }

    // ── Lời mời ─────────────────────────────────────────────────────────────

    /** Gửi lời mời (nếu người kia đã mời mình thì thành bạn luôn). Trả về quan hệ hiện tại. */
    public Map<String, Object> request(String me, String target) {
        UserEntity other = requireUser(target);
        if (other.getUsername().equalsIgnoreCase(me)) throw bad("Không thể kết bạn với chính mình");
        if ("ROLE_ADMIN".equals(role(other))) throw bad("Không thể kết bạn với tài khoản quản trị");
        KetBanEntity k = repo.findById(KetBanEntity.idOf(me, other.getUsername())).orElse(null);
        if (k != null) {
            if (!k.isFriend() && k.getNguoiNhan().equalsIgnoreCase(me)) return accept(me, other.getUsername());
            return relationResult(k, me, other);
        }
        if (relations(me).stream().filter(KetBanEntity::isFriend).count() >= MAX_FRIENDS) throw bad("Bạn đã đạt tối đa " + MAX_FRIENDS + " bạn bè");
        k = new KetBanEntity();
        k.setId(KetBanEntity.idOf(me, other.getUsername()));
        k.setNguoiGui(canonical(me));
        k.setNguoiNhan(other.getUsername());
        k.setTrangThai(KetBanEntity.CHO);
        k.setNgayTao(LocalDateTime.now());
        repo.save(k);
        if (thongBao != null) {
            thongBao.notify(other.getUsername(), ThongBaoEntity.KET_BAN_LOI_MOI, thongBao.displayName(me) + " muốn kết bạn với bạn",
                    "Chấp nhận để nhắn tin và rủ nhau ra sân.", ThongBaoEntity.FRIEND, canonical(me));
        }
        return relationResult(k, me, other);
    }

    public Map<String, Object> accept(String me, String from) {
        UserEntity other = requireUser(from);
        KetBanEntity k = repo.findById(KetBanEntity.idOf(me, other.getUsername()))
                .filter(x -> !x.isFriend() && x.getNguoiNhan().equalsIgnoreCase(me))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không có lời mời kết bạn này"));
        k.setTrangThai(KetBanEntity.BAN_BE);
        k.setNgayKetBan(LocalDateTime.now());
        repo.save(k);
        if (thongBao != null) {
            thongBao.notify(other.getUsername(), ThongBaoEntity.KET_BAN_DONG_Y, thongBao.displayName(me) + " đã đồng ý kết bạn",
                    "Giờ hai bạn có thể nhắn tin cho nhau.", ThongBaoEntity.CHAT, canonical(me));
        }
        return relationResult(k, me, other);
    }

    /** Từ chối lời mời, huỷ lời mời đã gửi hoặc huỷ kết bạn. */
    public void remove(String me, String other) {
        repo.deleteById(KetBanEntity.idOf(me, other == null ? "" : other.trim()));
    }

    // ── Quyền nhắn tin ──────────────────────────────────────────────────────

    public boolean areFriends(String a, String b) {
        return repo.findById(KetBanEntity.idOf(a, b)).map(KetBanEntity::isFriend).orElse(false);
    }

    /** Được mở cuộc trò chuyện mới với {@code other} không. */
    public boolean canStartChat(String me, String other) {
        if (areFriends(me, other)) return true;
        String r1 = userRepo.findByUsernameIgnoreCase(me).map(BanBeService::role).orElse("ROLE_USER");
        String r2 = userRepo.findByUsernameIgnoreCase(other).map(BanBeService::role).orElse("ROLE_USER");
        if (!"ROLE_USER".equals(r1) || !"ROLE_USER".equals(r2)) return true; // khách ↔ chủ sân / quản trị
        return shareGroup(me, other) || shareMatch(me, other);
    }

    /** Người có thể hiện trong danh bạ chat: bạn bè + người đã từng nhắn. */
    public List<Map<String, Object>> contacts(String me) {
        Set<String> names = new HashSet<>();
        relations(me).stream().filter(KetBanEntity::isFriend).forEach(k -> names.add(k.other(me).toLowerCase(Locale.ROOT)));
        Set<String> rooms = roomMemberRepo.findByUsername(me).stream().map(ThanhVienPhongChatEntity::getMaPhongChat).collect(Collectors.toSet());
        rooms.forEach(room -> roomMemberRepo.findByMaPhongChat(room).forEach(m -> names.add(m.getUsername().toLowerCase(Locale.ROOT))));
        names.remove(me.toLowerCase(Locale.ROOT));
        return names.stream()
                .map(n -> userRepo.findByUsernameIgnoreCase(n).filter(UserEntity::isActive).orElse(null))
                .filter(Objects::nonNull)
                .map(BanBeService::profile)
                .toList();
    }

    /** Hồ sơ công khai (để mở chat với chủ sân, người trong trận…). */
    public Map<String, Object> publicProfile(String me, String username) {
        UserEntity u = requireUser(username);
        Map<String, Object> item = profile(u);
        item.put("quanHe", relationOf(repo.findById(KetBanEntity.idOf(me, u.getUsername())).orElse(null), me));
        item.put("coTheNhan", u.getUsername().equalsIgnoreCase(me) ? false : canStartChat(me, u.getUsername()) || hasRoom(me, u.getUsername()));
        return item;
    }

    public boolean hasRoom(String a, String b) {
        Set<String> mine = roomMemberRepo.findByUsername(a).stream().map(ThanhVienPhongChatEntity::getMaPhongChat).collect(Collectors.toSet());
        return roomMemberRepo.findByUsername(b).stream().anyMatch(m -> mine.contains(m.getMaPhongChat()));
    }

    // ── Tiện ích ────────────────────────────────────────────────────────────

    private boolean shareGroup(String a, String b) {
        Set<String> mine = groupMemberRepo.findByUsername(a).stream()
                .filter(m -> !ThanhVienNhomEntity.CHO_DUYET.equals(m.getTrangThai()))
                .map(ThanhVienNhomEntity::getMaNhom).collect(Collectors.toSet());
        return !mine.isEmpty() && groupMemberRepo.findByUsername(b).stream()
                .filter(m -> !ThanhVienNhomEntity.CHO_DUYET.equals(m.getTrangThai()))
                .anyMatch(m -> mine.contains(m.getMaNhom()));
    }

    /** Cùng trận: đang chơi / chờ / xin vào (để chủ trận và người xin vào nhắn được cho nhau). */
    private boolean shareMatch(String a, String b) {
        Set<String> active = Set.of(ThamGiaTranEntity.THAM_GIA, ThamGiaTranEntity.CHO, ThamGiaTranEntity.CHO_DUYET);
        Set<String> mine = matchPlayerRepo.findByUsername(a).stream().filter(p -> active.contains(p.getTrangThai()))
                .map(ThamGiaTranEntity::getMaTran).collect(Collectors.toSet());
        return !mine.isEmpty() && matchPlayerRepo.findByUsername(b).stream()
                .filter(p -> active.contains(p.getTrangThai()))
                .anyMatch(p -> mine.contains(p.getMaTran()));
    }

    private List<KetBanEntity> relations(String me) {
        String name = canonical(me);
        return repo.findByNguoiGuiOrNguoiNhan(name, name);
    }

    /** BAN_BE, DA_GUI (mình đã mời), DUOC_MOI (người kia mời mình), KHONG. */
    private static String relationOf(KetBanEntity k, String me) {
        if (k == null) return "KHONG";
        if (k.isFriend()) return "BAN_BE";
        return k.getNguoiGui().equalsIgnoreCase(me) ? "DA_GUI" : "DUOC_MOI";
    }

    private static Map<String, Object> relationResult(KetBanEntity k, String me, UserEntity other) {
        Map<String, Object> item = profile(other);
        item.put("quanHe", relationOf(k, me));
        return item;
    }

    static Map<String, Object> profile(UserEntity u) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("username", u.getUsername());
        item.put("fullName", u.getFullName());
        item.put("role", role(u));
        return item;
    }

    static String role(UserEntity u) {
        String r = u.getRole() == null ? "" : u.getRole().trim().toUpperCase(Locale.ROOT);
        if (r.isEmpty()) return "ROLE_USER";
        return r.startsWith("ROLE_") ? r : "ROLE_" + r;
    }

    private String canonical(String username) {
        return userRepo.findByUsernameIgnoreCase(username).map(UserEntity::getUsername).orElse(username);
    }

    private UserEntity requireUser(String username) {
        return userRepo.findByUsernameIgnoreCase(username == null ? "" : username.trim())
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
