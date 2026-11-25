package com.example.alopoapi.service;

import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.nhom.ThamGiaTranEntity;
import com.example.alopoapi.entity.nhom.TranDauEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.NhomRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.ThamGiaTranRepo;
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

import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TranDauServiceTests {

    @Mock private TranDauRepo tranRepo;
    @Mock private ThamGiaTranRepo thamGiaRepo;
    @Mock private PhieuDatSanRepo phieuRepo;
    @Mock private SanRepo sanRepo;
    @Mock private CumSanRepo cumSanRepo;
    @Mock private NhomRepo nhomRepo;
    @Mock private UserRepo userRepo;
    @Mock private NhomService nhomService;
    @Mock private ManagementService managementService;

    private TranDauService service;
    private final Map<String, TranDauEntity> matches = new HashMap<>();
    private final Map<String, ThamGiaTranEntity> people = new HashMap<>();

    @BeforeEach
    void setUp() {
        service = new TranDauService(tranRepo, thamGiaRepo, phieuRepo, sanRepo, cumSanRepo, nhomRepo, userRepo, nhomService, managementService);
        // Repo giả lưu trong bộ nhớ.
        when(tranRepo.save(any())).thenAnswer(i -> { TranDauEntity m = i.getArgument(0); matches.put(m.getMaTran(), m); return m; });
        when(tranRepo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(matches.get(i.<String>getArgument(0))));
        when(thamGiaRepo.save(any())).thenAnswer(i -> { ThamGiaTranEntity p = i.getArgument(0); people.put(p.getId(), p); return p; });
        when(thamGiaRepo.saveAll(anyList())).thenAnswer(i -> { List<ThamGiaTranEntity> l = i.getArgument(0); l.forEach(p -> people.put(p.getId(), p)); return l; });
        when(thamGiaRepo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(people.get(i.<String>getArgument(0))));
        when(thamGiaRepo.findByMaTran(anyString())).thenAnswer(i -> new ArrayList<>(people.values().stream().filter(p -> p.getMaTran().equals(i.getArgument(0))).toList()));
        when(thamGiaRepo.findByMaTranIn(anyCollection())).thenAnswer(i -> people.values().stream().filter(p -> i.<java.util.Collection<String>>getArgument(0).contains(p.getMaTran())).toList());
        when(userRepo.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(sanRepo.findById("S1")).thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 120_000, true, "CS1")));
    }

    private PhieuDatSanEntity booking(String owner, String status, LocalDateTime start) {
        PhieuDatSanEntity b = new PhieuDatSanEntity("PD1", owner, "S1", start, start.plusHours(2), 240_000, start.minusDays(1), status);
        when(phieuRepo.findById("PD1")).thenReturn(Optional.of(b));
        return b;
    }

    private TranDauEntity createMatch(Map<String, Object> extra) {
        booking("host", "DA_XAC_NHAN", LocalDateTime.now().plusDays(1));
        Map<String, Object> body = new HashMap<>(Map.of("maPhieuDat", "PD1"));
        body.putAll(extra);
        Map<String, Object> result = service.create("host", body);
        return matches.get((String) result.get("maTran"));
    }

    @Test
    void createsPublicMatchFromOwnBookingWithSuggestedFee() {
        TranDauEntity match = createMatch(Map.of());

        assertTrue(match.isCongKhai(), "trận không thuộc nhóm luôn công khai");
        assertEquals(4, match.getSoNguoiToiDa());
        assertEquals(60_000, match.getChiPhiMoiNguoi()); // 240k / 4 người
        assertEquals("CS1", match.getMaCumSan());
        ThamGiaTranEntity host = people.get(ThamGiaTranEntity.idOf(match.getMaTran(), "host"));
        assertEquals(ThamGiaTranEntity.THAM_GIA, host.getTrangThai());
        assertEquals(ThamGiaTranEntity.MIEN, host.getThanhToan());
    }

    @Test
    void rejectsSomeoneElsesOrPastBooking() {
        booking("other", "DA_XAC_NHAN", LocalDateTime.now().plusDays(1));
        var forbidden = assertThrows(ResponseStatusException.class, () -> service.create("host", Map.of("maPhieuDat", "PD1")));
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());

        booking("host", "DA_XAC_NHAN", LocalDateTime.now().minusHours(1));
        var past = assertThrows(ResponseStatusException.class, () -> service.create("host", Map.of("maPhieuDat", "PD1")));
        assertEquals(HttpStatus.BAD_REQUEST, past.getStatusCode());

        booking("host", "DA_HUY", LocalDateTime.now().plusDays(1));
        assertThrows(ResponseStatusException.class, () -> service.create("host", Map.of("maPhieuDat", "PD1")));
    }

    @Test
    void onlyOneOpenMatchPerBooking() {
        booking("host", "CHO_XAC_NHAN", LocalDateTime.now().plusDays(1));
        when(tranRepo.existsByMaPhieuDatAndTrangThai("PD1", TranDauEntity.MO)).thenReturn(true);
        var e = assertThrows(ResponseStatusException.class, () -> service.create("host", Map.of("maPhieuDat", "PD1")));
        assertEquals("Lượt đặt sân này đã có trận", e.getReason());
    }

    @Test
    void outsiderRequestsToJoinAndHostApproves() {
        TranDauEntity match = createMatch(Map.of());
        service.join(match.getMaTran(), "guest", Map.of("loiNhan", "Cho mình vào với", "trinhDo", "TB"));
        ThamGiaTranEntity guest = people.get(ThamGiaTranEntity.idOf(match.getMaTran(), "guest"));
        assertEquals(ThamGiaTranEntity.CHO_DUYET, guest.getTrangThai());
        assertEquals(60_000, guest.getSoTien());

        service.review(match.getMaTran(), "host", "guest", true);
        assertEquals(ThamGiaTranEntity.THAM_GIA, guest.getTrangThai());
    }

    @Test
    void privateGroupMatchRejectsOutsiders() {
        when(nhomService.isMember("G1", "host")).thenReturn(true);
        TranDauEntity match = createMatch(Map.of("maNhom", "G1", "congKhai", false));
        assertFalse(match.isCongKhai());
        var e = assertThrows(ResponseStatusException.class, () -> service.join(match.getMaTran(), "guest", Map.of()));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }

    @Test
    void groupMembersGoToWaitlistWhenFullAndArePromotedWhenSomeoneLeaves() {
        when(nhomService.isMember(eq("G1"), anyString())).thenReturn(true);
        TranDauEntity match = createMatch(Map.of("maNhom", "G1", "soNguoiToiDa", 2));
        String id = match.getMaTran();

        service.join(id, "an", Map.of());
        service.join(id, "binh", Map.of());
        assertEquals(ThamGiaTranEntity.THAM_GIA, people.get(ThamGiaTranEntity.idOf(id, "an")).getTrangThai());
        assertEquals(ThamGiaTranEntity.CHO, people.get(ThamGiaTranEntity.idOf(id, "binh")).getTrangThai());

        service.leave(id, "an");
        assertEquals(ThamGiaTranEntity.VANG, people.get(ThamGiaTranEntity.idOf(id, "an")).getTrangThai());
        assertEquals(ThamGiaTranEntity.THAM_GIA, people.get(ThamGiaTranEntity.idOf(id, "binh")).getTrangThai());
    }

    @Test
    void hostCannotApproveWhenFullOrLeaveOwnMatch() {
        TranDauEntity match = createMatch(Map.of("soNguoiToiDa", 2));
        String id = match.getMaTran();
        service.join(id, "a", Map.of());
        service.join(id, "b", Map.of());
        service.review(id, "host", "a", true);

        var full = assertThrows(ResponseStatusException.class, () -> service.review(id, "host", "b", true));
        assertEquals(HttpStatus.BAD_REQUEST, full.getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.leave(id, "host"));
        var notHost = assertThrows(ResponseStatusException.class, () -> service.review(id, "a", "b", true));
        assertEquals(HttpStatus.FORBIDDEN, notHost.getStatusCode());
    }

    @Test
    void rejectedPlayerCannotRequestAgain() {
        TranDauEntity match = createMatch(Map.of());
        service.join(match.getMaTran(), "guest", Map.of());
        service.review(match.getMaTran(), "host", "guest", false);
        var e = assertThrows(ResponseStatusException.class, () -> service.join(match.getMaTran(), "guest", Map.of()));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }

    @Test
    void paymentFlow() {
        TranDauEntity match = createMatch(Map.of("nganHang", "MB", "soTaiKhoan", "0123 456 789", "chuTaiKhoan", "NGUYEN VAN A"));
        assertEquals("0123456789", match.getSoTaiKhoan());
        String id = match.getMaTran();
        service.join(id, "guest", Map.of());

        // Chưa được duyệt thì chưa báo trả tiền được.
        assertThrows(ResponseStatusException.class, () -> service.pay(id, "guest", Map.of()));
        service.review(id, "host", "guest", true);
        assertThrows(ResponseStatusException.class, () -> service.pay(id, "guest", Map.of("billUrl", "http://evil")));

        service.pay(id, "guest", Map.of("billUrl", "https://x.supabase.co/storage/v1/object/public/images/bill.jpg"));
        ThamGiaTranEntity guest = people.get(ThamGiaTranEntity.idOf(id, "guest"));
        assertEquals(ThamGiaTranEntity.CHO_XAC_NHAN, guest.getThanhToan());

        Map<String, Object> detail = service.confirmPayment(id, "host", "guest", true);
        assertEquals(ThamGiaTranEntity.DA_TRA, guest.getThanhToan());
        assertEquals(60_000L, detail.get("daThu"));
        assertThrows(ResponseStatusException.class, () -> service.pay(id, "host", Map.of()));
    }

    @Test
    void changingFeeUpdatesUnpaidPlayersOnly() {
        TranDauEntity match = createMatch(Map.of());
        String id = match.getMaTran();
        service.join(id, "a", Map.of());
        service.join(id, "b", Map.of());
        service.review(id, "host", "a", true);
        service.review(id, "host", "b", true);
        service.confirmPayment(id, "host", "b", true);

        service.update(id, "host", Map.of("chiPhiMoiNguoi", 50_000));
        assertEquals(50_000, people.get(ThamGiaTranEntity.idOf(id, "a")).getSoTien());
        assertEquals(60_000, people.get(ThamGiaTranEntity.idOf(id, "b")).getSoTien());
        var e = assertThrows(ResponseStatusException.class, () -> service.update(id, "host", Map.of("soNguoiToiDa", 2)));
        assertTrue(e.getReason().contains("3 người"));
    }

    @Test
    void cancelledMatchCannotBeJoined() {
        TranDauEntity match = createMatch(Map.of());
        service.cancel(match.getMaTran(), "host");
        assertEquals(TranDauEntity.DA_HUY, match.getTrangThai());
        assertThrows(ResponseStatusException.class, () -> service.join(match.getMaTran(), "guest", Map.of()));
    }

    @Test
    void feeAndLevelHelpers() {
        assertEquals(63_000, TranDauService.suggestedFee(250_000, 4));
        assertEquals(0, TranDauService.suggestedFee(0, 4));
        TranDauEntity m = new TranDauEntity();
        m.setTrinhDoTu("TB");
        m.setTrinhDoDen("KHA");
        assertTrue(TranDauService.fitsLevel(m, "TB_KHA"));
        assertFalse(TranDauService.fitsLevel(m, "YEU"));
        assertFalse(TranDauService.fitsLevel(m, "GIOI"));
        assertTrue(TranDauService.fitsLevel(new TranDauEntity(), "YEU"));
    }
}
