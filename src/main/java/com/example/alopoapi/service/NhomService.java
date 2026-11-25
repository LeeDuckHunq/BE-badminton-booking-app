package com.example.alopoapi.service;

import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.entity.chat.PhongChatEntity;
import com.example.alopoapi.entity.chat.TinNhanEntity;
import com.example.alopoapi.entity.nhom.NhomEntity;
import com.example.alopoapi.entity.nhom.ThanhVienNhomEntity;
import com.example.alopoapi.entity.nhom.TranDauEntity;
import com.example.alopoapi.repository.NhomRepo;
import com.example.alopoapi.repository.PhongChatRepo;
import com.example.alopoapi.repository.ThanhVienNhomRepo;
import com.example.alopoapi.repository.TinNhanRepo;
import com.example.alopoapi.repository.TranDauRepo;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.example.alopoapi.entity.nhom.ThanhVienNhomEntity.CHO_DUYET;
import static com.example.alopoapi.entity.nhom.ThanhVienNhomEntity.DA_VAO;
import static com.example.alopoapi.entity.nhom.ThanhVienNhomEntity.PHO_NHOM;
import static com.example.alopoapi.entity.nhom.ThanhVienNhomEntity.THANH_VIEN;
import static com.example.alopoapi.entity.nhom.ThanhVienNhomEntity.TRUONG_NHOM;
import static com.example.alopoapi.service.CommunityRules.bad;
import static com.example.alopoapi.service.CommunityRules.forbidden;
import static com.example.alopoapi.service.CommunityRules.get;
import static com.example.alopoapi.service.CommunityRules.notFound;

/** Hội nhóm: tạo nhóm, mời / duyệt thành viên, phân quyền và chat nhóm. */
@Service
public class NhomService {

    static final int MAX_MEMBERS = 200;
    static final int MAX_GROUPS_PER_USER = 20;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final NhomRepo nhomRepo;
    private final ThanhVienNhomRepo memberRepo;
    private final TranDauRepo tranDauRepo;
    private final PhongChatRepo phongChatRepo;
    private final TinNhanRepo tinNhanRepo;
    private final UserRepo userRepo;

    public NhomService(NhomRepo nhomRepo, ThanhVienNhomRepo memberRepo, TranDauRepo tranDauRepo,
                       PhongChatRepo phongChatRepo, TinNhanRepo tinNhanRepo, UserRepo userRepo) {
        this.nhomRepo = nhomRepo;
        this.memberRepo = memberRepo;
        this.tranDauRepo = tranDauRepo;
        this.phongChatRepo = phongChatRepo;
        this.tinNhanRepo = tinNhanRepo;
        this.userRepo = userRepo;
    }

    // ── Nhóm ─────────────────────────────────────────────────────────────────

    public Map<String, Object> create(String me, Map<String, Object> body) {
        long owned = memberRepo.findByUsername(me).stream().filter(ThanhVienNhomEntity::isActive).count();
        if (owned >= MAX_GROUPS_PER_USER) throw bad("Mỗi tài khoản tham gia tối đa " + MAX_GROUPS_PER_USER + " nhóm");

        LocalDateTime now = LocalDateTime.now();
        NhomEntity group = new NhomEntity();
        group.setMaNhom("NH" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        group.setNguoiTao(me);
        group.setNgayTao(now);
        group.setMaMoi(newInviteCode());
        applyInfo(group, body, true);
        nhomRepo.save(group);

        PhongChatEntity room = new PhongChatEntity(group.chatRoomId(), "GROUP", now, null, null);
        phongChatRepo.save(room);

        ThanhVienNhomEntity owner = member(group.getMaNhom(), me, TRUONG_NHOM, DA_VAO, null);
        memberRepo.save(owner);
        systemMessage(group, displayName(me) + " đã tạo nhóm. Chia sẻ mã mời " + group.getMaMoi() + " để rủ bạn bè vào nhóm nhé!");
        return detail(group.getMaNhom(), me);
    }

    public Map<String, Object> update(String id, String me, Map<String, Object> body) {
        NhomEntity group = requireUsable(id);
        requireLeader(id, me);
        applyInfo(group, body, false);
        nhomRepo.save(group);
        return detail(id, me);
    }

    /** Nhóm của tôi (đã vào và đang chờ duyệt), nhóm có tin mới nhất lên trước. */
    public List<Map<String, Object>> myGroups(String me) {
        List<ThanhVienNhomEntity> mine = memberRepo.findByUsername(me);
        if (mine.isEmpty()) return List.of();
        Map<String, NhomEntity> groups = new HashMap<>();
        nhomRepo.findAllById(mine.stream().map(ThanhVienNhomEntity::getMaNhom).toList()).forEach(g -> groups.put(g.getMaNhom(), g));
        Map<String, PhongChatEntity> rooms = new HashMap<>();
        phongChatRepo.findAllById(groups.values().stream().map(NhomEntity::chatRoomId).toList()).forEach(r -> rooms.put(r.getMaPhongChat(), r));
        Map<String, List<ThanhVienNhomEntity>> members = membersByGroup(groups.keySet().stream().toList());
        Map<String, Optional<UserEntity>> users = new HashMap<>();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ThanhVienNhomEntity m : mine) {
            NhomEntity g = groups.get(m.getMaNhom());
            if (g == null) continue;
            Map<String, Object> item = summary(g, members.getOrDefault(g.getMaNhom(), List.of()), m, users);
            PhongChatEntity room = rooms.get(g.chatRoomId());
            if (m.isActive() && room != null) {
                item.put("tinNhanCuoi", room.getTinNhanCuoi());
                item.put("thoiGianTinNhanCuoi", room.getThoiGianTinNhanCuoi());
            }
            result.add(item);
        }
        result.sort(Comparator.comparing((Map<String, Object> i) -> {
            Object t = i.get("thoiGianTinNhanCuoi");
            return t instanceof LocalDateTime d ? d : (LocalDateTime) i.get("ngayTao");
        }, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /** Nhóm công khai để khám phá (không gồm nhóm bị khoá). */
    public List<Map<String, Object>> discover(String query, String level, String me) {
        String q = query == null ? "" : normalize(query);
        String lv = CommunityRules.level(level);
        List<NhomEntity> groups = nhomRepo.findByCongKhaiTrueAndKhoaFalse().stream()
                .filter(g -> q.isEmpty() || normalize(g.getTenNhom() + " " + nz(g.getKhuVuc()) + " " + nz(g.getMoTa())).contains(q))
                .filter(g -> lv == null || g.getTrinhDo() == null || lv.equals(g.getTrinhDo()))
                .toList();
        if (groups.isEmpty()) return List.of();
        Map<String, List<ThanhVienNhomEntity>> members = membersByGroup(groups.stream().map(NhomEntity::getMaNhom).toList());
        Map<String, Optional<UserEntity>> users = new HashMap<>();
        return groups.stream()
                .map(g -> {
                    List<ThanhVienNhomEntity> list = members.getOrDefault(g.getMaNhom(), List.of());
                    ThanhVienNhomEntity mine = me == null ? null : list.stream().filter(m -> m.getUsername().equalsIgnoreCase(me)).findFirst().orElse(null);
                    return summary(g, list, mine, users);
                })
                .sorted(Comparator.comparing((Map<String, Object> i) -> (Long) i.get("soThanhVien")).reversed())
                .toList();
    }

    /**
     * Chi tiết nhóm. Thành viên thấy danh sách thành viên và mã mời; trưởng / phó nhóm thấy thêm yêu cầu tham gia.
     * Người ngoài chỉ xem được nhóm công khai.
     */
    public Map<String, Object> detail(String id, String me) {
        NhomEntity group = requireGroup(id);
        List<ThanhVienNhomEntity> members = memberRepo.findByMaNhom(id);
        ThanhVienNhomEntity mine = members.stream().filter(m -> m.getUsername().equalsIgnoreCase(me)).findFirst().orElse(null);
        boolean active = mine != null && mine.isActive();
        if (!active && !group.isCongKhai() && mine == null) throw forbidden("Nhóm riêng tư, bạn cần mã mời để tham gia");

        Map<String, Optional<UserEntity>> users = new HashMap<>();
        Map<String, Object> result = summary(group, members, mine, users);
        if (active) {
            result.put("maMoi", group.getMaMoi());
            result.put("thanhVien", members.stream().filter(ThanhVienNhomEntity::isActive)
                    .sorted(Comparator.comparing((ThanhVienNhomEntity m) -> roleOrder(m.getVaiTro())).thenComparing(ThanhVienNhomEntity::getNgayThamGia, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(m -> memberView(m, users)).toList());
            if (mine.isLeader()) {
                result.put("yeuCau", members.stream().filter(m -> CHO_DUYET.equals(m.getTrangThai()))
                        .sorted(Comparator.comparing(ThanhVienNhomEntity::getNgayThamGia, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(m -> memberView(m, users)).toList());
            }
            result.put("soTran", tranDauRepo.countByMaNhom(id));
        }
        return result;
    }

    // ── Tham gia / rời ───────────────────────────────────────────────────────

    /** Vào nhóm: đúng mã mời → vào ngay; nhóm công khai không có mã → gửi yêu cầu chờ duyệt. */
    public Map<String, Object> join(String id, String me, String code, String message) {
        NhomEntity group = requireUsable(id);
        ThanhVienNhomEntity existing = memberRepo.findById(ThanhVienNhomEntity.idOf(id, me)).orElse(null);
        if (existing != null && existing.isActive()) return detail(id, me);

        boolean validCode = code != null && code.trim().equalsIgnoreCase(group.getMaMoi());
        if (!validCode && !group.isCongKhai()) throw forbidden("Mã mời không đúng");
        if (validCode) {
            requireCapacity(id);
            long joined = memberRepo.findByUsername(me).stream().filter(ThanhVienNhomEntity::isActive).count();
            if (joined >= MAX_GROUPS_PER_USER) throw bad("Mỗi tài khoản tham gia tối đa " + MAX_GROUPS_PER_USER + " nhóm");
            memberRepo.save(member(id, me, THANH_VIEN, DA_VAO, null));
            systemMessage(group, displayName(me) + " đã tham gia nhóm");
        } else if (existing == null) {
            memberRepo.save(member(id, me, THANH_VIEN, CHO_DUYET, CommunityRules.text(message, 300, "Lời nhắn")));
        }
        return detail(id, me);
    }

    public Map<String, Object> joinByCode(String me, String code) {
        String c = code == null ? "" : code.trim().toUpperCase();
        NhomEntity group = nhomRepo.findByMaMoi(c).orElseThrow(() -> notFound("Không tìm thấy nhóm với mã mời này"));
        return join(group.getMaNhom(), me, c, null);
    }

    /** Trưởng / phó nhóm duyệt hoặc từ chối yêu cầu tham gia. */
    public Map<String, Object> review(String id, String me, String target, boolean accept) {
        NhomEntity group = requireUsable(id);
        requireLeader(id, me);
        ThanhVienNhomEntity request = memberRepo.findById(ThanhVienNhomEntity.idOf(id, canonical(target)))
                .filter(m -> CHO_DUYET.equals(m.getTrangThai()))
                .orElseThrow(() -> notFound("Không tìm thấy yêu cầu tham gia"));
        if (accept) {
            requireCapacity(id);
            request.setTrangThai(DA_VAO);
            request.setNgayThamGia(LocalDateTime.now());
            memberRepo.save(request);
            systemMessage(group, displayName(request.getUsername()) + " đã tham gia nhóm");
        } else {
            memberRepo.delete(request);
        }
        return detail(id, me);
    }

    /** Huỷ yêu cầu tham gia mà mình đã gửi. */
    public void cancelRequest(String id, String me) {
        memberRepo.findById(ThanhVienNhomEntity.idOf(id, me))
                .filter(m -> CHO_DUYET.equals(m.getTrangThai()))
                .ifPresent(memberRepo::delete);
    }

    /** Chỉ trưởng nhóm: đặt phó nhóm / thành viên, hoặc chuyển quyền trưởng nhóm. */
    public Map<String, Object> setRole(String id, String me, String target, String role) {
        NhomEntity group = requireGroup(id);
        ThanhVienNhomEntity owner = requireMember(id, me);
        if (!owner.isOwner()) throw forbidden("Chỉ trưởng nhóm được phân quyền");
        ThanhVienNhomEntity member = requireMember(id, canonical(target));
        if (member.getUsername().equals(owner.getUsername())) throw bad("Không thể đổi vai trò của chính mình");
        String r = role == null ? "" : role.trim().toUpperCase();
        switch (r) {
            case PHO_NHOM, THANH_VIEN -> member.setVaiTro(r);
            case TRUONG_NHOM -> {
                member.setVaiTro(TRUONG_NHOM);
                owner.setVaiTro(PHO_NHOM);
                memberRepo.save(owner);
                systemMessage(group, displayName(member.getUsername()) + " là trưởng nhóm mới");
            }
            default -> throw bad("Vai trò không hợp lệ");
        }
        memberRepo.save(member);
        return detail(id, me);
    }

    /** Trưởng nhóm mời bất kỳ ai ra; phó nhóm chỉ mời được thành viên thường. */
    public Map<String, Object> kick(String id, String me, String target) {
        NhomEntity group = requireGroup(id);
        ThanhVienNhomEntity leader = requireLeader(id, me);
        ThanhVienNhomEntity member = requireMember(id, canonical(target));
        if (member.getUsername().equals(leader.getUsername())) throw bad("Hãy dùng chức năng rời nhóm");
        if (TRUONG_NHOM.equals(member.getVaiTro())) throw forbidden("Không thể mời trưởng nhóm ra khỏi nhóm");
        if (!leader.isOwner() && PHO_NHOM.equals(member.getVaiTro())) throw forbidden("Phó nhóm không thể mời phó nhóm khác ra");
        memberRepo.delete(member);
        systemMessage(group, displayName(member.getUsername()) + " đã bị mời ra khỏi nhóm");
        return detail(id, me);
    }

    /** Rời nhóm. Trưởng nhóm phải chuyển quyền trước (trừ khi là người cuối cùng → giải tán nhóm). */
    public void leave(String id, String me) {
        NhomEntity group = requireGroup(id);
        ThanhVienNhomEntity member = requireMember(id, me);
        if (member.isOwner()) {
            long others = memberRepo.countByMaNhomAndTrangThai(id, DA_VAO) - 1;
            if (others > 0) throw bad("Hãy chuyển quyền trưởng nhóm cho người khác trước khi rời nhóm");
            disband(id, me);
            return;
        }
        memberRepo.delete(member);
        systemMessage(group, displayName(me) + " đã rời nhóm");
    }

    /** Trưởng nhóm giải tán: xoá nhóm, thành viên, chat; huỷ các trận sắp tới của nhóm. */
    public void disband(String id, String me) {
        NhomEntity group = requireGroup(id);
        if (!requireMember(id, me).isOwner()) throw forbidden("Chỉ trưởng nhóm được giải tán nhóm");
        LocalDateTime now = LocalDateTime.now();
        List<TranDauEntity> matches = tranDauRepo.findByMaNhom(id);
        for (TranDauEntity m : matches) {
            if (TranDauEntity.MO.equals(m.getTrangThai()) && m.getBatDau() != null && m.getBatDau().isAfter(now)) {
                m.setTrangThai(TranDauEntity.DA_HUY);
            }
            m.setMaNhom(null);
        }
        tranDauRepo.saveAll(matches);
        memberRepo.deleteByMaNhom(id);
        tinNhanRepo.deleteByMaPhongChat(group.chatRoomId());
        phongChatRepo.deleteById(group.chatRoomId());
        nhomRepo.delete(group);
    }

    public Map<String, Object> regenerateCode(String id, String me) {
        NhomEntity group = requireUsable(id);
        requireLeader(id, me);
        group.setMaMoi(newInviteCode());
        nhomRepo.save(group);
        return detail(id, me);
    }

    // ── Chat nhóm ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> messages(String id, String me) {
        NhomEntity group = requireGroup(id);
        requireMember(id, me);
        List<TinNhanEntity> list = new ArrayList<>(tinNhanRepo.findTop200ByMaPhongChatOrderByThoiGianGuiDesc(group.chatRoomId()));
        list.sort(Comparator.comparing(TinNhanEntity::getThoiGianGui, Comparator.nullsFirst(Comparator.naturalOrder())));
        Map<String, Optional<UserEntity>> users = new HashMap<>();
        return list.stream().map(m -> messageView(m, users)).toList();
    }

    public Map<String, Object> send(String id, String me, String content) {
        NhomEntity group = requireUsable(id);
        requireMember(id, me);
        String text = content == null ? "" : content.trim();
        if (text.isEmpty() || text.length() > MAX_MESSAGE_LENGTH) throw bad("Nội dung tin nhắn không hợp lệ");
        TinNhanEntity message = saveMessage(group, me, text);
        return messageView(message, new HashMap<>());
    }

    /** Tin hệ thống trong chat nhóm (người tham gia, trận mới...). Bỏ qua nếu nhóm không còn. */
    public void systemMessage(String maNhom, String text) {
        if (maNhom == null) return;
        nhomRepo.findById(maNhom).ifPresent(g -> systemMessage(g, text));
    }

    void systemMessage(NhomEntity group, String text) {
        saveMessage(group, CommunityRules.SYSTEM_SENDER, text);
    }

    private TinNhanEntity saveMessage(NhomEntity group, String sender, String text) {
        LocalDateTime now = LocalDateTime.now();
        TinNhanEntity message = new TinNhanEntity("TN_" + UUID.randomUUID(), group.chatRoomId(), sender, text, now);
        tinNhanRepo.save(message);
        PhongChatEntity room = phongChatRepo.findById(group.chatRoomId())
                .orElseGet(() -> new PhongChatEntity(group.chatRoomId(), "GROUP", now, null, null));
        room.setTinNhanCuoi(CommunityRules.SYSTEM_SENDER.equals(sender) ? text : displayName(sender) + ": " + text);
        room.setThoiGianTinNhanCuoi(now);
        phongChatRepo.save(room);
        return message;
    }

    // ── Quản trị ─────────────────────────────────────────────────────────────

    public List<Map<String, Object>> adminList() {
        List<NhomEntity> groups = nhomRepo.findAll();
        Map<String, List<ThanhVienNhomEntity>> members = membersByGroup(groups.stream().map(NhomEntity::getMaNhom).toList());
        Map<String, Optional<UserEntity>> users = new HashMap<>();
        return groups.stream()
                .sorted(Comparator.comparing(NhomEntity::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(g -> {
                    Map<String, Object> item = summary(g, members.getOrDefault(g.getMaNhom(), List.of()), null, users);
                    item.put("soTran", tranDauRepo.countByMaNhom(g.getMaNhom()));
                    item.put("nguoiTao", g.getNguoiTao());
                    return item;
                }).toList();
    }

    public Map<String, Object> setLocked(String id, boolean locked) {
        NhomEntity group = requireGroup(id);
        group.setKhoa(locked);
        nhomRepo.save(group);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("maNhom", id);
        result.put("khoa", locked);
        return result;
    }

    // ── Dùng chung với TranDauService ────────────────────────────────────────

    public NhomEntity requireGroup(String id) {
        return nhomRepo.findById(id == null ? "" : id).orElseThrow(() -> notFound("Không tìm thấy nhóm"));
    }

    /** Nhóm tồn tại và không bị khoá. */
    public NhomEntity requireUsable(String id) {
        NhomEntity group = requireGroup(id);
        if (group.isKhoa()) throw forbidden("Nhóm đã bị khoá do vi phạm quy định");
        return group;
    }

    public boolean isMember(String maNhom, String username) {
        return maNhom != null && username != null && memberRepo.findById(ThanhVienNhomEntity.idOf(maNhom, username))
                .map(ThanhVienNhomEntity::isActive).orElse(false);
    }

    public ThanhVienNhomEntity requireMember(String id, String username) {
        return memberRepo.findById(ThanhVienNhomEntity.idOf(id, username))
                .filter(ThanhVienNhomEntity::isActive)
                .orElseThrow(() -> forbidden("Bạn không phải thành viên nhóm này"));
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private ThanhVienNhomEntity requireLeader(String id, String me) {
        ThanhVienNhomEntity m = requireMember(id, me);
        if (!m.isLeader()) throw forbidden("Chỉ trưởng nhóm hoặc phó nhóm được làm việc này");
        return m;
    }

    private void requireCapacity(String id) {
        if (memberRepo.countByMaNhomAndTrangThai(id, DA_VAO) >= MAX_MEMBERS) throw bad("Nhóm đã đủ " + MAX_MEMBERS + " thành viên");
    }

    private void applyInfo(NhomEntity group, Map<String, Object> body, boolean creating) {
        if (creating || body.containsKey("tenNhom")) group.setTenNhom(CommunityRules.required(get(body, "tenNhom"), 3, 60, "Tên nhóm"));
        if (creating || body.containsKey("moTa")) group.setMoTa(CommunityRules.text(get(body, "moTa"), 500, "Giới thiệu"));
        if (creating || body.containsKey("khuVuc")) group.setKhuVuc(CommunityRules.text(get(body, "khuVuc"), 80, "Khu vực"));
        if (creating || body.containsKey("trinhDo")) group.setTrinhDo(CommunityRules.level(get(body, "trinhDo")));
        if (creating || body.containsKey("congKhai")) group.setCongKhai(CommunityRules.bool(get(body, "congKhai"), true));
        if (creating || body.containsKey("mau")) {
            Integer color = CommunityRules.integer(get(body, "mau"));
            group.setMau(color == null ? Math.floorMod(group.getMaNhom().hashCode(), 6) : Math.floorMod(color, 6));
        }
    }

    private String newInviteCode() {
        for (int i = 0; i < 20; i++) {
            String code = CommunityRules.inviteCode();
            if (!nhomRepo.existsByMaMoi(code)) return code;
        }
        throw new IllegalStateException("Không tạo được mã mời");
    }

    private ThanhVienNhomEntity member(String maNhom, String username, String role, String status, String message) {
        ThanhVienNhomEntity m = new ThanhVienNhomEntity();
        m.setId(ThanhVienNhomEntity.idOf(maNhom, username));
        m.setMaNhom(maNhom);
        m.setUsername(username);
        m.setVaiTro(role);
        m.setTrangThai(status);
        m.setLoiNhan(message);
        m.setNgayThamGia(LocalDateTime.now());
        return m;
    }

    private Map<String, List<ThanhVienNhomEntity>> membersByGroup(List<String> ids) {
        Map<String, List<ThanhVienNhomEntity>> result = new HashMap<>();
        if (ids.isEmpty()) return result;
        memberRepo.findByMaNhomIn(ids).forEach(m -> result.computeIfAbsent(m.getMaNhom(), k -> new ArrayList<>()).add(m));
        return result;
    }

    private Map<String, Object> summary(NhomEntity g, List<ThanhVienNhomEntity> members, ThanhVienNhomEntity mine, Map<String, Optional<UserEntity>> users) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("maNhom", g.getMaNhom());
        item.put("tenNhom", g.getTenNhom());
        item.put("moTa", g.getMoTa());
        item.put("khuVuc", g.getKhuVuc());
        item.put("trinhDo", g.getTrinhDo());
        item.put("congKhai", g.isCongKhai());
        item.put("mau", g.getMau());
        item.put("khoa", g.isKhoa());
        item.put("ngayTao", g.getNgayTao());
        item.put("soThanhVien", members.stream().filter(ThanhVienNhomEntity::isActive).count());
        members.stream().filter(ThanhVienNhomEntity::isOwner).findFirst().ifPresent(owner -> {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("username", owner.getUsername());
            o.put("ten", CommunityRules.fullName(user(owner.getUsername(), users), owner.getUsername()));
            item.put("truongNhom", o);
        });
        item.put("thanhVienTieuBieu", members.stream().filter(ThanhVienNhomEntity::isActive)
                .sorted(Comparator.comparing((ThanhVienNhomEntity m) -> roleOrder(m.getVaiTro())))
                .limit(5)
                .map(m -> CommunityRules.fullName(user(m.getUsername(), users), m.getUsername()))
                .toList());
        if (mine != null) {
            Map<String, Object> me = new LinkedHashMap<>();
            me.put("vaiTro", mine.getVaiTro());
            me.put("trangThai", mine.getTrangThai());
            item.put("toi", me);
        } else {
            item.put("toi", null);
        }
        return item;
    }

    private Map<String, Object> memberView(ThanhVienNhomEntity m, Map<String, Optional<UserEntity>> users) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("username", m.getUsername());
        item.put("ten", CommunityRules.fullName(user(m.getUsername(), users), m.getUsername()));
        item.put("vaiTro", m.getVaiTro());
        item.put("trangThai", m.getTrangThai());
        item.put("loiNhan", m.getLoiNhan());
        item.put("ngayThamGia", m.getNgayThamGia());
        return item;
    }

    private Map<String, Object> messageView(TinNhanEntity m, Map<String, Optional<UserEntity>> users) {
        boolean system = CommunityRules.SYSTEM_SENDER.equals(m.getUsernameNguoiGui());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("maTinNhan", m.getMaTinNhan());
        item.put("usernameNguoiGui", system ? null : m.getUsernameNguoiGui());
        item.put("tenNguoiGui", system ? null : CommunityRules.fullName(user(m.getUsernameNguoiGui(), users), m.getUsernameNguoiGui()));
        item.put("noiDung", m.getNoiDung());
        item.put("thoiGianGui", m.getThoiGianGui());
        item.put("heThong", system);
        return item;
    }

    private UserEntity user(String username, Map<String, Optional<UserEntity>> cache) {
        return cache.computeIfAbsent(username, userRepo::findByUsernameIgnoreCase).orElse(null);
    }

    private String displayName(String username) {
        return CommunityRules.fullName(userRepo.findByUsernameIgnoreCase(username).orElse(null), username);
    }

    /** Username đúng chữ hoa/thường như trong DB (id thành viên dùng username gốc). */
    private String canonical(String username) {
        if (username == null) throw bad("Thiếu tài khoản");
        return userRepo.findByUsernameIgnoreCase(username.trim()).map(UserEntity::getUsername).orElse(username.trim());
    }

    private static int roleOrder(String role) {
        return switch (role == null ? "" : role) {
            case TRUONG_NHOM -> 0;
            case PHO_NHOM -> 1;
            default -> 2;
        };
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Bỏ dấu, chữ thường để tìm kiếm. */
    static String normalize(String s) {
        String n = java.text.Normalizer.normalize(s.toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').trim();
    }
}
