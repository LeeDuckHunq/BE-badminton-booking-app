package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.HoaDonRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhieuDatSanServiceTests {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @Mock
    private PhieuDatSanRepo bookingRepository;

    @Mock
    private SanRepo courtRepository;

    @Mock
    private CumSanRepo venueRepository;

    @Mock
    private HoaDonRepo invoiceRepository;

    @Mock
    private BookingLockService bookingLockService;

    private PhieuDatSanService service;

    @BeforeEach
    void setUp() {
        service = new PhieuDatSanService(
                bookingRepository,
                courtRepository,
                venueRepository,
                invoiceRepository,
                bookingLockService
        );
    }

    @Test
    void createsBookingForAuthenticatedUserAndAlwaysReleasesCourtLock() {
        CreatePhieuDatRequest request = validRequest();
        request.setMaNguoiDung("someone-else");
        stubActiveCourt();
        when(bookingLockService.tryAcquire(eq("S1"), any())).thenReturn(true);
        when(bookingRepository.existsConflict("S1", request.getBatDau(), request.getKetThuc()))
                .thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PhieuDatSanEntity booking = service.createPhieuDat(request, "user1");

        assertEquals("S1", booking.getMaSan());
        assertEquals("user1", booking.getMaNguoiDung());
        assertEquals(150_000, booking.getTongTien());
        assertEquals("CHO_XAC_NHAN", booking.getTrangThai());
        verify(bookingLockService).release(eq("S1"), any());
    }

    @Test
    void rejectsOverlappingBookingAndReleasesCourtLock() {
        CreatePhieuDatRequest request = validRequest();
        stubActiveCourt();
        when(bookingLockService.tryAcquire(eq("S1"), any())).thenReturn(true);
        when(bookingRepository.existsConflict("S1", request.getBatDau(), request.getKetThuc()))
                .thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request, "user1"));

        verify(bookingRepository, never()).save(any());
        verify(bookingLockService).release(eq("S1"), any());
    }

    @Test
    void rejectsInvalidTimeBeforeAccessingMongoDb() {
        CreatePhieuDatRequest request = validRequest();
        request.setKetThuc(request.getBatDau());

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request, "user1"));

        verify(courtRepository, never()).findById(any());
        verify(bookingLockService, never()).tryAcquire(any(), any());
    }

    @Test
    void rejectsBookingInThePast() {
        CreatePhieuDatRequest request = validRequest();
        request.setBatDau(LocalDateTime.now().minusDays(1));
        request.setKetThuc(request.getBatDau().plusHours(1));

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request, "user1"));
        verify(courtRepository, never()).findById(any());
    }

    @Test
    void rejectsBookingOutsideOpeningHours() {
        CreatePhieuDatRequest request = validRequest();
        request.setBatDau(TOMORROW.atTime(5, 0));
        request.setKetThuc(TOMORROW.atTime(7, 0));
        stubActiveCourt();

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request, "user1"));
        verify(bookingLockService, never()).tryAcquire(any(), any());
    }

    @Test
    void rejectsInactiveCourt() {
        CreatePhieuDatRequest request = validRequest();
        when(courtRepository.findById("S1"))
                .thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 100_000, false, "CS1")));

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request, "user1"));
        verify(bookingLockService, never()).tryAcquire(any(), any());
    }

    @Test
    void onlyOwnerCanDeletePendingUnpaidBooking() {
        PhieuDatSanEntity booking = pendingBooking();
        when(bookingRepository.findById("PD1")).thenReturn(Optional.of(booking));

        ResponseStatusException forbidden = assertThrows(ResponseStatusException.class,
                () -> service.deletePhieuDat("PD1", "attacker"));
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
        verify(bookingRepository, never()).deleteById(any());

        when(invoiceRepository.existsByMaPhieuDat("PD1")).thenReturn(false);
        service.deletePhieuDat("PD1", "user1");
        verify(bookingRepository).deleteById("PD1");
    }

    @Test
    void cannotDeleteBookingThatAlreadyHasInvoice() {
        when(bookingRepository.findById("PD1")).thenReturn(Optional.of(pendingBooking()));
        when(invoiceRepository.existsByMaPhieuDat("PD1")).thenReturn(true);

        ResponseStatusException conflict = assertThrows(ResponseStatusException.class,
                () -> service.deletePhieuDat("PD1", "user1"));
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        verify(bookingRepository, never()).deleteById(any());
    }

    @Test
    void walkInBookingIsConfirmedImmediatelyAndMarkedAsVenueBooking() {
        CreatePhieuDatRequest request = validRequest();
        stubActiveCourt();
        when(bookingLockService.tryAcquire(eq("S1"), any())).thenReturn(true);
        when(bookingRepository.existsConflict("S1", request.getBatDau(), request.getKetThuc())).thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PhieuDatSanEntity booking = service.createWalkIn(request, b -> {
            b.setTenKhachHang("Khách vãng lai");
            b.setPhuongThucThanhToan("TIEN_MAT");
        });

        assertEquals("DA_XAC_NHAN", booking.getTrangThai());
        assertEquals("TAI_SAN", booking.getNguonDat());
        assertEquals("TIEN_MAT", booking.getPhuongThucThanhToan());
        assertNull(booking.getMaNguoiDung());
        assertEquals(150_000, booking.getTongTien());
        verify(bookingLockService).release(eq("S1"), any());
    }

    @Test
    void walkInMayBookTheSlotInProgressButAppBookingMayNot() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        CreatePhieuDatRequest request = new CreatePhieuDatRequest();
        request.setMaSan("S1");
        request.setBatDau(now.minusMinutes(20));
        request.setKetThuc(now.plusMinutes(40));

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request, "user1"));

        when(courtRepository.findById("S1"))
                .thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 100_000, true, "CS1")));
        // Giờ mở cửa cả ngày để test không phụ thuộc thời điểm chạy.
        when(venueRepository.findById("CS1")).thenReturn(Optional.of(new CumSanEntity("CS1", "Cụm 1", "Địa chỉ", "", "", null, null)));
        when(bookingLockService.tryAcquire(eq("S1"), any())).thenReturn(true);
        when(bookingRepository.existsConflict(any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PhieuDatSanEntity booking = service.createWalkIn(request, b -> b.setTenKhachHang("Khách"));
        assertEquals("DA_XAC_NHAN", booking.getTrangThai());
    }

    @Test
    void walkInStillRejectsOverlaps() {
        CreatePhieuDatRequest request = validRequest();
        stubActiveCourt();
        when(bookingLockService.tryAcquire(eq("S1"), any())).thenReturn(true);
        when(bookingRepository.existsConflict("S1", request.getBatDau(), request.getKetThuc())).thenReturn(true);

        ResponseStatusException conflict = assertThrows(ResponseStatusException.class, () -> service.createWalkIn(request, b -> {}));
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void publicScheduleHidesCustomer() {
        when(bookingRepository.findByBatDauBetween(any(), any())).thenReturn(List.of(pendingBooking()));

        List<PhieuDatSanEntity> schedule = service.getPhieuDatTheoNgay(TOMORROW);

        assertEquals(1, schedule.size());
        assertNull(schedule.get(0).getMaNguoiDung());
        assertEquals("S1", schedule.get(0).getMaSan());
    }

    private void stubActiveCourt() {
        when(courtRepository.findById("S1"))
                .thenReturn(Optional.of(new SanEntity("S1", "Sân 1", 100_000, true, "CS1")));
        CumSanEntity venue = new CumSanEntity("CS1", "Cụm 1", "Địa chỉ", "", "", LocalTime.of(6, 0), LocalTime.of(22, 0));
        when(venueRepository.findById("CS1")).thenReturn(Optional.of(venue));
    }

    private PhieuDatSanEntity pendingBooking() {
        return new PhieuDatSanEntity("PD1", "user1", "S1",
                TOMORROW.atTime(8, 0), TOMORROW.atTime(9, 0), 100_000,
                LocalDateTime.now(), "CHO_XAC_NHAN");
    }

    private CreatePhieuDatRequest validRequest() {
        CreatePhieuDatRequest request = new CreatePhieuDatRequest();
        request.setMaSan("S1");
        request.setBatDau(TOMORROW.atTime(8, 0));
        request.setKetThuc(TOMORROW.atTime(9, 30));
        return request;
    }
}
