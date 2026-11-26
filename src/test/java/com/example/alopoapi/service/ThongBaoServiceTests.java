package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.ThongBaoEntity;
import com.example.alopoapi.entity.nhom.ThamGiaTranEntity;
import com.example.alopoapi.entity.nhom.TranDauEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.ThamGiaTranRepo;
import com.example.alopoapi.repository.ThongBaoRepo;
import com.example.alopoapi.repository.TranDauRepo;
import com.example.alopoapi.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ThongBaoServiceTests {

    @Mock private ThongBaoRepo repo;
    @Mock private SanRepo sanRepo;
    @Mock private CumSanRepo cumSanRepo;
    @Mock private UserRepo userRepo;
    @Mock private TranDauRepo tranRepo;
    @Mock private ThamGiaTranRepo thamGiaRepo;

    private ThongBaoService service;
    private final Map<String, ThongBaoEntity> store = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        service = new ThongBaoService(repo, sanRepo, cumSanRepo, userRepo, tranRepo, thamGiaRepo);
        when(repo.save(any())).thenAnswer(i -> { ThongBaoEntity t = i.getArgument(0); store.put(t.getMaThongBao(), t); return t; });
        when(repo.saveAll(anyList())).thenAnswer(i -> { List<ThongBaoEntity> l = i.getArgument(0); l.forEach(t -> store.put(t.getMaThongBao(), t)); return l; });
        when(repo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(store.get(i.<String>getArgument(0))));
        when(repo.findFirstByNguoiNhanAndLoaiAndNguoiGuiAndDaDocFalse(anyString(), anyString(), anyString())).thenAnswer(i -> store.values().stream()
                .filter(t -> t.getNguoiNhan().equals(i.getArgument(0)) && t.getLoai().equals(i.getArgument(1))
                        && i.getArgument(2).equals(t.getNguoiGui()) && !t.isDaDoc())
                .findFirst());
        when(repo.findByNguoiNhanAndDaGuiFalseAndThoiGianAfterOrderByThoiGianAsc(anyString(), any())).thenAnswer(i -> store.values().stream()
                .filter(t -> t.getNguoiNhan().equals(i.getArgument(0)) && !t.isDaGui()).toList());
        when(userRepo.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());
        CumSanEntity venue = new CumSanEntity("CS1", "Cụm Quận 1", "12 Nguyễn Huệ", null, "0908 123 456", null, null);
        venue.setOwnerUsername("owner");
        when(cumSanRepo.findById("CS1")).thenReturn(Optional.of(venue));
        when(sanRepo.findById("S1")).thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 100_000, true, "CS1")));
        when(sanRepo.findById("S2")).thenReturn(Optional.of(new SanEntity("S2", "Sân 2", 100_000, true, "CS1")));
    }

    private PhieuDatSanEntity booking(String status) {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
        return new PhieuDatSanEntity("PD_1", "khach", "S1", start, start.plusMinutes(90), 210_000, LocalDateTime.now(), status);
    }

    @Test
    void messagesFromSameSenderCollapseIntoOneUnreadNotification() {
        service.message("khach", "owner", "Chào bạn");
        service.message("khach", "owner", "Sân 2 hôm nay có đèn mới nhé");
        assertEquals(1, store.size());
        ThongBaoEntity tb = store.values().iterator().next();
        assertEquals(2, tb.getSoLuong());
        assertEquals("Sân 2 hôm nay có đèn mới nhé", tb.getNoiDung());
        assertTrue(tb.getTieuDe().contains("2 tin nhắn mới"));
        assertEquals(ThongBaoEntity.CHAT, tb.getDoiTuong());
        assertEquals("owner", tb.getMaDoiTuong());
    }

    @Test
    void doesNotNotifySenderAboutOwnMessage() {
        service.message("owner", "owner", "test");
        assertTrue(store.isEmpty());
    }

    @Test
    void takePendingReturnsOnceThenMarksDelivered() {
        service.notify("khach", ThongBaoEntity.DAT_SAN_XAC_NHAN, "A", "B", ThongBaoEntity.BOOKING, "PD_1");
        assertEquals(1, service.takePending("khach").size());
        assertEquals(0, service.takePending("khach").size());
        assertFalse(store.values().iterator().next().isDaDoc());
    }

    @Test
    void cannotMarkSomeoneElsesNotificationRead() {
        service.notify("khach", ThongBaoEntity.DAT_SAN_XAC_NHAN, "A", "B", ThongBaoEntity.BOOKING, "PD_1");
        String id = store.keySet().iterator().next();
        assertThrows(ResponseStatusException.class, () -> service.markRead(id, "nguoi-khac"));
        service.markRead(id, "khach");
        assertTrue(store.get(id).isDaDoc());
    }

    @Test
    void confirmedBookingNotifiesCustomerWithCourtAndTime() {
        PhieuDatSanEntity b = booking("DA_XAC_NHAN");
        service.bookingStatusChanged(b, "CHO_XAC_NHAN");
        ThongBaoEntity tb = store.values().iterator().next();
        assertEquals("khach", tb.getNguoiNhan());
        assertEquals(ThongBaoEntity.DAT_SAN_XAC_NHAN, tb.getLoai());
        assertTrue(tb.getNoiDung().startsWith("Sân 1 · Cụm Quận 1, 18:00–19:30"));
    }

    @Test
    void unchangedStatusSendsNothing() {
        service.bookingStatusChanged(booking("DA_XAC_NHAN"), "DA_XAC_NHAN");
        assertTrue(store.isEmpty());
    }

    @Test
    void cancellingBookingCancelsItsOpenMatchAndNotifiesPlayers() {
        TranDauEntity match = new TranDauEntity();
        match.setMaTran("TR1");
        match.setMaPhieuDat("PD_1");
        match.setChuTran("khach");
        match.setTieuDe("Kèo tối");
        match.setTrangThai(TranDauEntity.MO);
        when(tranRepo.findByMaPhieuDatInAndTrangThai(eq(List.of("PD_1")), eq(TranDauEntity.MO))).thenReturn(List.of(match));
        List<ThamGiaTranEntity> people = new ArrayList<>();
        for (String u : List.of("khach", "ban1", "ban2")) {
            ThamGiaTranEntity p = new ThamGiaTranEntity();
            p.setMaTran("TR1");
            p.setUsername(u);
            p.setTrangThai(ThamGiaTranEntity.THAM_GIA);
            people.add(p);
        }
        when(thamGiaRepo.findByMaTran("TR1")).thenReturn(people);

        service.bookingStatusChanged(booking("DA_HUY"), "DA_XAC_NHAN");

        assertEquals(TranDauEntity.DA_HUY, match.getTrangThai());
        List<String> matchNotified = store.values().stream().filter(t -> ThongBaoEntity.TRAN_HUY.equals(t.getLoai())).map(ThongBaoEntity::getNguoiNhan).toList();
        assertEquals(List.of("ban1", "ban2"), matchNotified);
        assertTrue(store.values().stream().anyMatch(t -> ThongBaoEntity.DAT_SAN_HUY.equals(t.getLoai()) && "khach".equals(t.getNguoiNhan())));
    }

    @Test
    void courtChangeNotifiesCustomerAndMovesMatch() {
        TranDauEntity match = new TranDauEntity();
        match.setMaTran("TR1");
        match.setMaSan("S1");
        match.setTieuDe("Kèo tối");
        match.setTrangThai(TranDauEntity.MO);
        when(tranRepo.findByMaPhieuDatInAndTrangThai(eq(List.of("PD_1")), eq(TranDauEntity.MO))).thenReturn(List.of(match));
        ThamGiaTranEntity friend = new ThamGiaTranEntity();
        friend.setMaTran("TR1");
        friend.setUsername("ban1");
        friend.setTrangThai(ThamGiaTranEntity.THAM_GIA);
        when(thamGiaRepo.findByMaTran("TR1")).thenReturn(List.of(friend));

        PhieuDatSanEntity b = booking("DA_XAC_NHAN");
        b.setMaSan("S2");
        service.courtChanged(b, new SanEntity("S1", "Sân 1", 100_000, true, "CS1"), new SanEntity("S2", "Sân 2", 100_000, true, "CS1"), "Sàn sân 1 bị ướt");

        assertEquals("S2", match.getMaSan());
        ThongBaoEntity customer = store.values().stream().filter(t -> "khach".equals(t.getNguoiNhan())).findFirst().orElseThrow();
        assertEquals(ThongBaoEntity.DAT_SAN_DOI_SAN, customer.getLoai());
        assertTrue(customer.getNoiDung().contains("từ Sân 1 sang Sân 2"));
        assertTrue(customer.getNoiDung().contains("Sàn sân 1 bị ướt"));
        assertTrue(store.values().stream().anyMatch(t -> "ban1".equals(t.getNguoiNhan()) && ThongBaoEntity.TRAN_DOI_SAN.equals(t.getLoai())));
    }

    @Test
    void invoiceNotifiesVenueOwner() {
        service.invoiceSubmitted(booking("CHO_XAC_NHAN"), 210_000);
        ThongBaoEntity tb = store.values().iterator().next();
        assertEquals("owner", tb.getNguoiNhan());
        assertEquals(ThongBaoEntity.DON_MOI, tb.getLoai());
        assertTrue(tb.getNoiDung().contains("210.000 đ"));
    }
}
