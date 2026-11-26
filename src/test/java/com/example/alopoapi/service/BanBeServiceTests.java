package com.example.alopoapi.service;

import com.example.alopoapi.entity.KetBanEntity;
import com.example.alopoapi.entity.ThongBaoEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.entity.nhom.ThanhVienNhomEntity;
import com.example.alopoapi.repository.KetBanRepo;
import com.example.alopoapi.repository.ThamGiaTranRepo;
import com.example.alopoapi.repository.ThanhVienNhomRepo;
import com.example.alopoapi.repository.ThanhVienPhongChatRepo;
import com.example.alopoapi.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BanBeServiceTests {

    @Mock private KetBanRepo repo;
    @Mock private UserRepo userRepo;
    @Mock private ThanhVienPhongChatRepo roomRepo;
    @Mock private ThanhVienNhomRepo groupRepo;
    @Mock private ThamGiaTranRepo matchRepo;
    @Mock private ThongBaoService thongBao;

    private BanBeService service;
    private final Map<String, KetBanEntity> store = new HashMap<>();
    private final Map<String, UserEntity> users = new HashMap<>();

    private void user(String username, String name, String phone, String role) {
        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setFullName(name);
        u.setPhoneNumber(phone);
        u.setRole(role);
        u.setActive(true);
        users.put(username.toLowerCase(), u);
    }

    @BeforeEach
    void setUp() {
        service = new BanBeService(repo, userRepo, roomRepo, groupRepo, matchRepo);
        service.setThongBaoService(thongBao);
        user("tuananh", "Nguyễn Tuấn Anh", "0908 123 456", "ROLE_USER");
        user("minhkhang", "Lê Minh Khang", "0912 345 678", "ROLE_USER");
        user("mailinh", "Trần Mai Linh", "0933 222 111", "ROLE_USER");
        user("hoanganh", "Hoàng Văn Anh", "0988 777 666", "ROLE_OWNER");
        user("admin", "Quản trị", null, "ROLE_ADMIN");
        when(userRepo.findByUsernameIgnoreCase(anyString())).thenAnswer(i -> Optional.ofNullable(users.get(i.<String>getArgument(0).toLowerCase())));
        when(userRepo.findAll()).thenAnswer(i -> new ArrayList<>(users.values()));
        when(repo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(store.get(i.<String>getArgument(0))));
        when(repo.save(any())).thenAnswer(i -> { KetBanEntity k = i.getArgument(0); store.put(k.getId(), k); return k; });
        doAnswer(i -> store.remove(i.<String>getArgument(0))).when(repo).deleteById(anyString());
        when(repo.findByNguoiGuiOrNguoiNhan(anyString(), anyString())).thenAnswer(i -> store.values().stream()
                .filter(k -> k.getNguoiGui().equalsIgnoreCase(i.getArgument(0)) || k.getNguoiNhan().equalsIgnoreCase(i.getArgument(0))).toList());
        when(groupRepo.findByUsername(anyString())).thenReturn(List.of());
        when(matchRepo.findByUsername(anyString())).thenReturn(List.of());
        when(roomRepo.findByUsername(anyString())).thenReturn(List.of());
        when(thongBao.displayName(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void requestThenAcceptMakesFriendsAndNotifiesBothWays() {
        Map<String, Object> sent = service.request("tuananh", "minhkhang");
        assertEquals("DA_GUI", sent.get("quanHe"));
        verify(thongBao).notify(eq("minhkhang"), eq(ThongBaoEntity.KET_BAN_LOI_MOI), anyString(), anyString(), eq(ThongBaoEntity.FRIEND), eq("tuananh"));
        assertFalse(service.areFriends("tuananh", "minhkhang"));

        Map<String, Object> accepted = service.accept("minhkhang", "tuananh");
        assertEquals("BAN_BE", accepted.get("quanHe"));
        assertTrue(service.areFriends("minhkhang", "TuanAnh"));
        verify(thongBao).notify(eq("tuananh"), eq(ThongBaoEntity.KET_BAN_DONG_Y), anyString(), anyString(), eq(ThongBaoEntity.CHAT), eq("minhkhang"));

        Map<String, Object> list = service.overview("tuananh");
        assertEquals(1, ((List<?>) list.get("banBe")).size());
    }

    @Test
    void inviteBackAutoAcceptsAndSenderCannotAcceptOwnRequest() {
        service.request("tuananh", "mailinh");
        assertThrows(ResponseStatusException.class, () -> service.accept("tuananh", "mailinh"));
        assertEquals("BAN_BE", service.request("mailinh", "tuananh").get("quanHe"));
    }

    @Test
    void cannotFriendSelfOrAdmin() {
        assertThrows(ResponseStatusException.class, () -> service.request("tuananh", "tuananh"));
        assertThrows(ResponseStatusException.class, () -> service.request("tuananh", "admin"));
    }

    @Test
    void removeHandlesDeclineCancelAndUnfriend() {
        service.request("tuananh", "minhkhang");
        service.remove("minhkhang", "tuananh");
        assertTrue(store.isEmpty());
    }

    @Test
    void strangersCannotStartChatButFriendsOwnersAndGroupmatesCan() {
        assertFalse(service.canStartChat("tuananh", "mailinh"));
        assertTrue(service.canStartChat("tuananh", "hoanganh"));
        assertTrue(service.canStartChat("hoanganh", "mailinh"));

        ThanhVienNhomEntity a = new ThanhVienNhomEntity();
        a.setMaNhom("NH1");
        a.setUsername("tuananh");
        a.setTrangThai(ThanhVienNhomEntity.DA_VAO);
        ThanhVienNhomEntity b = new ThanhVienNhomEntity();
        b.setMaNhom("NH1");
        b.setUsername("mailinh");
        b.setTrangThai(ThanhVienNhomEntity.DA_VAO);
        when(groupRepo.findByUsername("tuananh")).thenReturn(List.of(a));
        when(groupRepo.findByUsername("mailinh")).thenReturn(List.of(b));
        assertTrue(service.canStartChat("tuananh", "mailinh"));

        b.setTrangThai(ThanhVienNhomEntity.CHO_DUYET); // mới xin vào nhóm thì chưa tính
        assertFalse(service.canStartChat("tuananh", "mailinh"));

        service.request("tuananh", "mailinh");
        service.accept("mailinh", "tuananh");
        assertTrue(service.canStartChat("tuananh", "mailinh"));
    }

    @Test
    void searchNeedsThreeCharsOrFullPhoneAndHidesAdminAndSelf() {
        assertTrue(service.search("tuananh", "ma").isEmpty());
        List<Map<String, Object>> byName = service.search("tuananh", "anh");
        assertTrue(byName.stream().noneMatch(m -> "tuananh".equals(m.get("username"))));
        assertTrue(byName.stream().anyMatch(m -> "hoanganh".equals(m.get("username"))));
        assertTrue(service.search("tuananh", "quản").isEmpty());

        List<Map<String, Object>> byPhone = service.search("tuananh", "0912345678");
        assertEquals(1, byPhone.size());
        assertEquals("minhkhang", byPhone.get(0).get("username"));
        assertFalse(byPhone.get(0).containsKey("phoneNumber"));
        assertEquals("KHONG", byPhone.get(0).get("quanHe"));

        assertTrue(service.search("tuananh", "0912").isEmpty()); // số lẻ không đủ → không dò được
    }
}
