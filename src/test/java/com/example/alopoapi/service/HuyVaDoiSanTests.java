package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.ThongBaoEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.HoaDonRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Khách xin huỷ sân đã xác nhận, chủ sân duyệt huỷ / đổi sân. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HuyVaDoiSanTests {

    @Mock private PhieuDatSanRepo bookingRepo;
    @Mock private SanRepo sanRepo;
    @Mock private CumSanRepo cumSanRepo;
    @Mock private HoaDonRepo hoaDonRepo;
    @Mock private UserRepo userRepo;
    @Mock private BookingLockService lockService;
    @Mock private ThongBaoService thongBao;
    @Mock private ChatService chatService;

    private PhieuDatSanService bookings;
    private ManagementService management;
    private PhieuDatSanEntity booking;
    private final Authentication owner = new UsernamePasswordAuthenticationToken("owner", null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

    @BeforeEach
    void setUp() {
        bookings = new PhieuDatSanService(bookingRepo, sanRepo, cumSanRepo, hoaDonRepo, lockService);
        bookings.setThongBaoService(thongBao);
        bookings.setChatService(chatService);
        management = new ManagementService(cumSanRepo, sanRepo, bookingRepo, userRepo, hoaDonRepo, bookings);
        management.setThongBaoService(thongBao);

        CumSanEntity venue = new CumSanEntity("CS1", "Cụm Quận 1", "12 Nguyễn Huệ", null, "+84 908 123 456", null, null);
        venue.setOwnerUsername("owner");
        venue.setTrangThai("ACTIVE");
        CumSanEntity other = new CumSanEntity("CS2", "Cụm Thủ Đức", "1 Võ Văn Ngân", null, null, null, null);
        other.setOwnerUsername("owner");
        when(cumSanRepo.findById("CS1")).thenReturn(Optional.of(venue));
        when(cumSanRepo.findById("CS2")).thenReturn(Optional.of(other));
        when(sanRepo.findById("S1")).thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 100_000, true, "CS1")));
        when(sanRepo.findById("S2")).thenReturn(Optional.of(new SanEntity("S2", "Sân 2", 100_000, true, "CS1")));
        when(sanRepo.findById("S9")).thenReturn(Optional.of(new SanEntity("S9", "Sân 9", 100_000, true, "CS2")));
        when(bookingRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(lockService.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(chatService.getOrCreateRoom("khach", "owner")).thenReturn("ROOM");
        when(thongBao.describe(any())).thenReturn("Sân 1 · Cụm Quận 1, 18:00–19:30 T6 27/11");
        when(thongBao.displayName(anyString())).thenAnswer(i -> i.getArgument(0));

        booking = new PhieuDatSanEntity("PD_abc12345-xyz", "khach", "S1", LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(4), 200_000, LocalDateTime.now().minusDays(1), "DA_XAC_NHAN");
        when(bookingRepo.findById("PD_abc12345-xyz")).thenReturn(Optional.of(booking));
    }

    private int status(Runnable r) {
        return assertThrows(ResponseStatusException.class, r::run).getStatusCode().value();
    }

    // ── Khách xin huỷ ────────────────────────────────────────────────────────

    @Test
    void customerRequestsCancelMessagesOwnerAndReturnsZaloContact() {
        Map<String, Object> result = bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc đột xuất");

        assertEquals(PhieuDatSanService.HUY_CHO_XU_LY, booking.getYeuCauHuy());
        assertEquals("Bận việc đột xuất", booking.getLyDoHuy());
        assertEquals("DA_XAC_NHAN", booking.getTrangThai()); // chỉ chủ sân mới huỷ được
        @SuppressWarnings("unchecked") Map<String, Object> contact = (Map<String, Object>) result.get("lienHe");
        assertEquals("owner", contact.get("chuSan"));
        assertEquals("https://zalo.me/0908123456", contact.get("zalo"));
        verify(chatService).sendMessage(any(), eq("khach"), eq(false));
        verify(thongBao).notify(eq("owner"), eq(ThongBaoEntity.YEU_CAU_HUY), anyString(), contains("Bận việc đột xuất"), eq(ThongBaoEntity.BOOKING), eq(booking.getMaPhieuDat()));
    }

    @Test
    void cannotRequestCancelWithin30MinutesOfStart() {
        booking.setBatDau(LocalDateTime.now().plusMinutes(20));
        assertEquals(400, status(() -> bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc")));
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void unpaidPendingBookingMustBeCancelledAtCheckoutInstead() {
        booking.setTrangThai("CHO_XAC_NHAN");
        when(hoaDonRepo.existsByMaPhieuDat(booking.getMaPhieuDat())).thenReturn(false);
        assertEquals(400, status(() -> bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc")));
    }

    @Test
    void paidPendingBookingCanRequestCancel() {
        booking.setTrangThai("CHO_XAC_NHAN");
        when(hoaDonRepo.existsByMaPhieuDat(booking.getMaPhieuDat())).thenReturn(true);
        bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc");
        assertEquals(PhieuDatSanService.HUY_CHO_XU_LY, booking.getYeuCauHuy());
    }

    @Test
    void onlyBookingOwnerCanRequestCancelAndReasonIsRequired() {
        assertEquals(HttpStatus.FORBIDDEN.value(), status(() -> bookings.requestCancel(booking.getMaPhieuDat(), "nguoi-khac", "Bận việc")));
        assertEquals(400, status(() -> bookings.requestCancel(booking.getMaPhieuDat(), "khach", "  ")));
    }

    @Test
    void customerCanWithdrawPendingRequest() {
        bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc");
        bookings.withdrawCancel(booking.getMaPhieuDat(), "khach");
        assertEquals(null, booking.getYeuCauHuy());
        assertEquals(400, status(() -> bookings.withdrawCancel(booking.getMaPhieuDat(), "khach")));
    }

    // ── Chủ sân xử lý ────────────────────────────────────────────────────────

    @Test
    void ownerAcceptsCancelRequestReleasesSlot() {
        bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc");
        management.handleCancelRequest(booking.getMaPhieuDat(), true, "Đã hoàn tiền qua MB", owner);
        assertEquals("DA_HUY", booking.getTrangThai());
        assertEquals(PhieuDatSanService.HUY_DONG_Y, booking.getYeuCauHuy());
        verify(thongBao).bookingStatusChanged(booking, "DA_XAC_NHAN");
    }

    @Test
    void ownerRejectNeedsReasonAndKeepsBooking() {
        bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc");
        assertEquals(400, status(() -> management.handleCancelRequest(booking.getMaPhieuDat(), false, " ", owner)));
        management.handleCancelRequest(booking.getMaPhieuDat(), false, "Sát giờ, sân không hoàn tiền", owner);
        assertEquals("DA_XAC_NHAN", booking.getTrangThai());
        assertEquals(PhieuDatSanService.HUY_TU_CHOI, booking.getYeuCauHuy());
        verify(thongBao).notify(eq("khach"), eq(ThongBaoEntity.HUY_BI_TU_CHOI), anyString(), contains("không hoàn tiền"), anyString(), anyString());
    }

    @Test
    void cancellingViaStatusAlsoClosesPendingRequest() {
        bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc");
        management.updateBookingStatus(booking.getMaPhieuDat(), "DA_HUY", owner);
        assertEquals(PhieuDatSanService.HUY_DONG_Y, booking.getYeuCauHuy());
    }

    @Test
    void otherOwnerCannotHandleRequest() {
        bookings.requestCancel(booking.getMaPhieuDat(), "khach", "Bận việc");
        Authentication stranger = new UsernamePasswordAuthenticationToken("ke-khac", null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
        assertEquals(403, status(() -> management.handleCancelRequest(booking.getMaPhieuDat(), true, null, stranger)));
    }

    // ── Đổi sân ─────────────────────────────────────────────────────────────

    @Test
    void ownerMovesBookingToFreeCourtInSameVenue() {
        when(bookingRepo.existsConflict(eq("S2"), any(), any())).thenReturn(false);
        management.changeCourt(booking.getMaPhieuDat(), "S2", "Đèn sân 1 hỏng", owner);
        assertEquals("S2", booking.getMaSan());
        assertEquals("S1", booking.getMaSanCu());
        assertEquals("Đèn sân 1 hỏng", booking.getLyDoDoiSan());
        assertEquals(200_000, booking.getTongTien());
        verify(thongBao).courtChanged(eq(booking), any(), any(), eq("Đèn sân 1 hỏng"));
        verify(lockService).release(eq("S2"), anyString());
    }

    @Test
    void cannotMoveToBusyCourt() {
        when(bookingRepo.existsConflict(eq("S2"), any(), any())).thenReturn(true);
        assertEquals(409, status(() -> management.changeCourt(booking.getMaPhieuDat(), "S2", "Đèn hỏng", owner)));
        assertEquals("S1", booking.getMaSan());
        verify(lockService).release(eq("S2"), anyString());
    }

    @Test
    void cannotMoveToAnotherVenueOrWithoutReason() {
        assertEquals(400, status(() -> management.changeCourt(booking.getMaPhieuDat(), "S9", "Đèn hỏng", owner)));
        assertEquals(400, status(() -> management.changeCourt(booking.getMaPhieuDat(), "S2", "", owner)));
        assertEquals(400, status(() -> management.changeCourt(booking.getMaPhieuDat(), "S1", "Đèn hỏng", owner)));
    }

    @Test
    void cannotMoveFinishedOrCancelledBooking() {
        booking.setTrangThai("DA_HUY");
        assertEquals(400, status(() -> management.changeCourt(booking.getMaPhieuDat(), "S2", "Đèn hỏng", owner)));
        booking.setTrangThai("DA_XAC_NHAN");
        booking.setKetThuc(LocalDateTime.now().minusMinutes(5));
        assertEquals(400, status(() -> management.changeCourt(booking.getMaPhieuDat(), "S2", "Đèn hỏng", owner)));
    }
}
