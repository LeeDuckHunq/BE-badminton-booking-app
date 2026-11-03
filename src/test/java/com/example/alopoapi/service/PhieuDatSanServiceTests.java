package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.repository.SanRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhieuDatSanServiceTests {

    @Mock
    private PhieuDatSanRepo bookingRepository;

    @Mock
    private SanRepo courtRepository;

    @Mock
    private BookingLockService bookingLockService;

    private PhieuDatSanService service;

    @BeforeEach
    void setUp() {
        service = new PhieuDatSanService(
                bookingRepository,
                courtRepository,
                bookingLockService
        );
    }

    @Test
    void createsBookingAndAlwaysReleasesCourtLock() {
        CreatePhieuDatRequest request = validRequest();
        SanEntity court = new SanEntity("S1", "Sân 1", 100_000, true, "CS1");

        when(courtRepository.findById("S1")).thenReturn(Optional.of(court));
        when(bookingLockService.tryAcquire(eq("S1"), any())).thenReturn(true);
        when(bookingRepository.existsConflict("S1", request.getBatDau(), request.getKetThuc()))
                .thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PhieuDatSanEntity booking = service.createPhieuDat(request);

        assertEquals("S1", booking.getMaSan());
        assertEquals("user1", booking.getMaNguoiDung());
        assertEquals(150_000, booking.getTongTien());
        assertEquals("CHO_XAC_NHAN", booking.getTrangThai());
        verify(bookingLockService).release(eq("S1"), any());
    }

    @Test
    void rejectsOverlappingBookingAndReleasesCourtLock() {
        CreatePhieuDatRequest request = validRequest();
        SanEntity court = new SanEntity("S1", "Sân 1", 100_000, true, "CS1");

        when(courtRepository.findById("S1")).thenReturn(Optional.of(court));
        when(bookingLockService.tryAcquire(eq("S1"), any())).thenReturn(true);
        when(bookingRepository.existsConflict("S1", request.getBatDau(), request.getKetThuc()))
                .thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request));

        verify(bookingRepository, never()).save(any());
        verify(bookingLockService).release(eq("S1"), any());
    }

    @Test
    void rejectsInvalidTimeBeforeAccessingMongoDb() {
        CreatePhieuDatRequest request = validRequest();
        request.setKetThuc(request.getBatDau());

        assertThrows(ResponseStatusException.class, () -> service.createPhieuDat(request));

        verify(courtRepository, never()).findById(any());
        verify(bookingLockService, never()).tryAcquire(any(), any());
    }

    private CreatePhieuDatRequest validRequest() {
        CreatePhieuDatRequest request = new CreatePhieuDatRequest();
        request.setMaNguoiDung("user1");
        request.setMaSan("S1");
        request.setBatDau(LocalDateTime.of(2026, 11, 4, 8, 0));
        request.setKetThuc(LocalDateTime.of(2026, 11, 4, 9, 30));
        return request;
    }
}
