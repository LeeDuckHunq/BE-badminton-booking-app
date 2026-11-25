package com.example.alopoapi.service;

import com.example.alopoapi.entity.nhom.NhomEntity;
import com.example.alopoapi.entity.nhom.ThanhVienNhomEntity;
import com.example.alopoapi.repository.NhomRepo;
import com.example.alopoapi.repository.PhongChatRepo;
import com.example.alopoapi.repository.ThanhVienNhomRepo;
import com.example.alopoapi.repository.TinNhanRepo;
import com.example.alopoapi.repository.TranDauRepo;
import com.example.alopoapi.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NhomServiceTests {

    @Mock private NhomRepo nhomRepo;
    @Mock private ThanhVienNhomRepo memberRepo;
    @Mock private TranDauRepo tranDauRepo;
    @Mock private PhongChatRepo phongChatRepo;
    @Mock private TinNhanRepo tinNhanRepo;
    @Mock private UserRepo userRepo;

    private NhomService service;
    private final Map<String, NhomEntity> groups = new HashMap<>();
    private final Map<String, ThanhVienNhomEntity> members = new HashMap<>();

    @BeforeEach
    void setUp() {
        service = new NhomService(nhomRepo, memberRepo, tranDauRepo, phongChatRepo, tinNhanRepo, userRepo);
        when(nhomRepo.save(any())).thenAnswer(i -> { NhomEntity g = i.getArgument(0); groups.put(g.getMaNhom(), g); return g; });
        when(nhomRepo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(groups.get(i.<String>getArgument(0))));
        when(nhomRepo.findByMaMoi(anyString())).thenAnswer(i -> groups.values().stream().filter(g -> g.getMaMoi().equals(i.getArgument(0))).findFirst());
        when(memberRepo.save(any())).thenAnswer(i -> { ThanhVienNhomEntity m = i.getArgument(0); members.put(m.getId(), m); return m; });
        when(memberRepo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(members.get(i.<String>getArgument(0))));
        when(memberRepo.findByMaNhom(anyString())).thenAnswer(i -> members.values().stream().filter(m -> m.getMaNhom().equals(i.getArgument(0))).toList());
        when(memberRepo.findByMaNhomIn(anyCollection())).thenAnswer(i -> members.values().stream().filter(m -> i.<java.util.Collection<String>>getArgument(0).contains(m.getMaNhom())).toList());
        when(memberRepo.findByUsername(anyString())).thenAnswer(i -> members.values().stream().filter(m -> m.getUsername().equals(i.getArgument(0))).toList());
        when(memberRepo.countByMaNhomAndTrangThai(anyString(), anyString())).thenAnswer(i -> members.values().stream()
                .filter(m -> m.getMaNhom().equals(i.getArgument(0)) && m.getTrangThai().equals(i.getArgument(1))).count());
        doAnswer(i -> members.remove(i.<ThanhVienNhomEntity>getArgument(0).getId())).when(memberRepo).delete(any());
        when(userRepo.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());
    }

    private NhomEntity create(boolean isPublic) {
        Map<String, Object> result = service.create("owner", Map.of("tenNhom", "  CLB Cầu lông Quận 7 ", "congKhai", isPublic, "trinhDo", "tb"));
        return groups.get((String) result.get("maNhom"));
    }

    @Test
    void creatorBecomesOwnerAndGetsInviteCode() {
        NhomEntity group = create(true);
        assertEquals("CLB Cầu lông Quận 7", group.getTenNhom());
        assertEquals("TB", group.getTrinhDo());
        assertNotNull(group.getMaMoi());
        assertEquals(6, group.getMaMoi().length());
        assertTrue(members.get(ThanhVienNhomEntity.idOf(group.getMaNhom(), "owner")).isOwner());
        verify(tinNhanRepo, atLeastOnce()).save(any());
    }

    @Test
    void privateGroupRequiresInviteCode() {
        NhomEntity group = create(false);
        var e = assertThrows(ResponseStatusException.class, () -> service.join(group.getMaNhom(), "an", null, null));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());

        service.joinByCode("an", group.getMaMoi().toLowerCase());
        assertTrue(members.get(ThanhVienNhomEntity.idOf(group.getMaNhom(), "an")).isActive());
    }

    @Test
    void publicGroupRequestNeedsLeaderApproval() {
        NhomEntity group = create(true);
        service.join(group.getMaNhom(), "an", null, "Mình chơi TB");
        ThanhVienNhomEntity request = members.get(ThanhVienNhomEntity.idOf(group.getMaNhom(), "an"));
        assertFalse(request.isActive());
        assertEquals("Mình chơi TB", request.getLoiNhan());

        // Thành viên thường không duyệt được.
        service.joinByCode("binh", group.getMaMoi());
        assertThrows(ResponseStatusException.class, () -> service.review(group.getMaNhom(), "binh", "an", true));

        service.review(group.getMaNhom(), "owner", "an", true);
        assertTrue(members.get(ThanhVienNhomEntity.idOf(group.getMaNhom(), "an")).isActive());
    }

    @Test
    void ownerMustTransferBeforeLeaving() {
        NhomEntity group = create(true);
        service.joinByCode("an", group.getMaMoi());
        var e = assertThrows(ResponseStatusException.class, () -> service.leave(group.getMaNhom(), "owner"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

        service.setRole(group.getMaNhom(), "owner", "an", ThanhVienNhomEntity.TRUONG_NHOM);
        assertTrue(members.get(ThanhVienNhomEntity.idOf(group.getMaNhom(), "an")).isOwner());
        assertEquals(ThanhVienNhomEntity.PHO_NHOM, members.get(ThanhVienNhomEntity.idOf(group.getMaNhom(), "owner")).getVaiTro());
        service.leave(group.getMaNhom(), "owner");
        assertFalse(members.containsKey(ThanhVienNhomEntity.idOf(group.getMaNhom(), "owner")));
    }

    @Test
    void viceLeaderCannotKickAnotherViceOrOwner() {
        NhomEntity group = create(true);
        service.joinByCode("an", group.getMaMoi());
        service.joinByCode("binh", group.getMaMoi());
        service.joinByCode("chi", group.getMaMoi());
        service.setRole(group.getMaNhom(), "owner", "an", ThanhVienNhomEntity.PHO_NHOM);
        service.setRole(group.getMaNhom(), "owner", "binh", ThanhVienNhomEntity.PHO_NHOM);

        assertThrows(ResponseStatusException.class, () -> service.kick(group.getMaNhom(), "an", "binh"));
        assertThrows(ResponseStatusException.class, () -> service.kick(group.getMaNhom(), "an", "owner"));
        service.kick(group.getMaNhom(), "an", "chi");
        assertFalse(members.containsKey(ThanhVienNhomEntity.idOf(group.getMaNhom(), "chi")));
    }

    @Test
    void lockedGroupBlocksChatAndJoining() {
        NhomEntity group = create(true);
        service.setLocked(group.getMaNhom(), true);
        assertThrows(ResponseStatusException.class, () -> service.send(group.getMaNhom(), "owner", "hello"));
        assertThrows(ResponseStatusException.class, () -> service.join(group.getMaNhom(), "an", group.getMaMoi(), null));
    }

    @Test
    void outsiderCannotReadGroupChat() {
        NhomEntity group = create(true);
        var e = assertThrows(ResponseStatusException.class, () -> service.messages(group.getMaNhom(), "stranger"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        when(tinNhanRepo.findTop200ByMaPhongChatOrderByThoiGianGuiDesc(anyString())).thenReturn(List.of());
        assertTrue(service.messages(group.getMaNhom(), "owner").isEmpty());
    }

    @Test
    void searchIgnoresVietnameseAccents() {
        assertEquals("cau long quan 7", NhomService.normalize("Cầu Lông Quận 7"));
        assertEquals("dong da", NhomService.normalize("Đống Đa"));
    }
}
