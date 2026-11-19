package com.example.alopoapi.service;

import com.example.alopoapi.entity.DanhGiaEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.DanhGiaRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import com.example.alopoapi.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DanhGiaServiceTests {

    @Mock private DanhGiaRepo danhGiaRepo;
    @Mock private PhieuDatSanRepo phieuDatSanRepo;
    @Mock private SanRepo sanRepo;
    @Mock private UserRepo userRepo;
    @Mock private ManagementService managementService;

    private DanhGiaService service;

    @BeforeEach
    void setUp() {
        service = new DanhGiaService(danhGiaRepo, phieuDatSanRepo, sanRepo, userRepo, managementService);
    }

    private PhieuDatSanEntity booking(String status, LocalDateTime end) {
        return new PhieuDatSanEntity("PD1", "user1", "S1", end.minusHours(1), end, 100_000, end.minusDays(1), status);
    }

    @Test
    void customerReviewsCompletedBooking() {
        when(phieuDatSanRepo.findById("PD1")).thenReturn(Optional.of(booking("DA_HOAN_THANH", LocalDateTime.now().minusDays(1))));
        when(sanRepo.findById("S1")).thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 100_000, true, "CS1")));
        when(danhGiaRepo.findById("DG_PD1")).thenReturn(Optional.empty());
        when(danhGiaRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        DanhGiaEntity review = service.submit("user1", "PD1", 5, "  Sân đẹp, đèn sáng  ");

        assertEquals("DG_PD1", review.getMaDanhGia());
        assertEquals("CS1", review.getMaCumSan());
        assertEquals(5, review.getSoSao());
        assertEquals("Sân đẹp, đèn sáng", review.getNoiDung());
    }

    @Test
    void confirmedBookingCanBeReviewedOnlyAfterPlayTime() {
        when(phieuDatSanRepo.findById("PD1")).thenReturn(Optional.of(booking("DA_XAC_NHAN", LocalDateTime.now().plusHours(2))));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.submit("user1", "PD1", 4, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(danhGiaRepo, never()).save(any());
    }

    @Test
    void cannotReviewSomeoneElsesBooking() {
        when(phieuDatSanRepo.findById("PD1")).thenReturn(Optional.of(booking("DA_HOAN_THANH", LocalDateTime.now().minusDays(1))));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.submit("attacker", "PD1", 1, "xấu"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void rejectsOutOfRangeStars() {
        assertThrows(ResponseStatusException.class, () -> service.submit("user1", "PD1", 0, null));
        assertThrows(ResponseStatusException.class, () -> service.submit("user1", "PD1", 6, null));
        verify(phieuDatSanRepo, never()).findById(any());
    }

    @Test
    void editingReviewKeepsOwnerReply() {
        DanhGiaEntity existing = new DanhGiaEntity();
        existing.setMaDanhGia("DG_PD1");
        existing.setMaPhieuDat("PD1");
        existing.setMaNguoiDung("user1");
        existing.setSoSao(2);
        existing.setNgayTao(LocalDateTime.now().minusDays(1));
        existing.setPhanHoi("Cảm ơn bạn, sân sẽ khắc phục.");
        when(phieuDatSanRepo.findById("PD1")).thenReturn(Optional.of(booking("DA_HOAN_THANH", LocalDateTime.now().minusDays(2))));
        when(sanRepo.findById("S1")).thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 100_000, true, "CS1")));
        when(danhGiaRepo.findById("DG_PD1")).thenReturn(Optional.of(existing));
        when(danhGiaRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        DanhGiaEntity review = service.submit("user1", "PD1", 4, "Đã cải thiện");

        assertEquals(4, review.getSoSao());
        assertEquals("Cảm ơn bạn, sân sẽ khắc phục.", review.getPhanHoi());
        assertNotNull(review.getNgayCapNhat());
    }

    @Test
    void publicNameHidesUsername() {
        assertEquals("Tuấn Anh N.", DanhGiaService.displayName(new UserEntity("tuananh", "x", "ROLE_USER", "Nguyễn Tuấn Anh", null, null)));
        assertEquals("Lan T.", DanhGiaService.displayName(new UserEntity("lan", "x", "ROLE_USER", "Trần Lan", null, null)));
        assertEquals("Người chơi SmashZone", DanhGiaService.displayName(null));
    }
}
