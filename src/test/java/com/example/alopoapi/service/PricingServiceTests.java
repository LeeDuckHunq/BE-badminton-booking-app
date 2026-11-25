package com.example.alopoapi.service;

import com.example.alopoapi.entity.KhungGia;
import com.example.alopoapi.entity.SanEntity;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PricingServiceTests {

    private static final List<Integer> WEEKDAYS = List.of(1, 2, 3, 4, 5);
    private static final List<Integer> WEEKEND = List.of(6, 7);

    /** Sân giá mặc định 100k; T2–T6 17:00–22:00 giá 150k; T7–CN cả ngày 130k. */
    private SanEntity court() {
        SanEntity s = new SanEntity("S1", "Sân 1", 100_000, true, "CS1");
        s.setBangGia(new ArrayList<>(List.of(
                new KhungGia(new ArrayList<>(WEEKDAYS), "17:00", "22:00", 150_000, "Cao điểm"),
                new KhungGia(new ArrayList<>(WEEKEND), "05:00", "24:00", 130_000, "Cuối tuần"))));
        return s;
    }

    // 2026-11-26 là Thứ Năm, 2026-11-28 là Thứ Bảy.
    private static LocalDateTime thu5(int h, int m) { return LocalDateTime.of(2026, 11, 26, h, m); }
    private static LocalDateTime thu7(int h, int m) { return LocalDateTime.of(2026, 11, 28, h, m); }

    @Test
    void offPeakUsesDefaultPrice() {
        assertEquals(150_000, PricingService.total(court(), thu5(8, 0), thu5(9, 30)));
    }

    @Test
    void peakHoursUseRulePrice() {
        assertEquals(300_000, PricingService.total(court(), thu5(18, 0), thu5(20, 0)));
    }

    @Test
    void bookingAcrossBoundaryIsSplit() {
        // 16:00–17:00 giá 100k + 17:00–18:00 giá 150k
        assertEquals(250_000, PricingService.total(court(), thu5(16, 0), thu5(18, 0)));
        // 21:30–22:30: 30 phút 150k + 30 phút 100k
        assertEquals(125_000, PricingService.total(court(), thu5(21, 30), thu5(22, 30)));
    }

    @Test
    void weekendRuleAndMidnightEnd() {
        assertEquals(195_000, PricingService.total(court(), thu7(6, 0), thu7(7, 30)));
        assertEquals(130_000, PricingService.total(court(), thu7(23, 0), thu7(23, 0).plusHours(1)));
    }

    @Test
    void courtWithoutRulesKeepsOldBehaviour() {
        SanEntity plain = new SanEntity("S2", "Sân 2", 120_000, true, "CS1");
        assertEquals(180_000, PricingService.total(plain, thu5(18, 0), thu5(19, 30)));
        assertEquals(120_000, PricingService.minRate(plain));
    }

    @Test
    void minRateConsidersRules() {
        SanEntity s = court();
        s.getBangGia().add(new KhungGia(new ArrayList<>(WEEKDAYS), "05:00", "07:00", 70_000, "Sáng sớm"));
        assertEquals(70_000, PricingService.minRate(s));
    }

    @Test
    void normalizeSortsAndCleans() {
        List<KhungGia> rules = PricingService.normalize(List.of(
                new KhungGia(new ArrayList<>(List.of(7, 6, 6)), "5:00", "24:00", 130_000.4, "  Cuối tuần "),
                new KhungGia(new ArrayList<>(List.of(1)), "17:00", "22:00", 150_000, "")));
        assertEquals(List.of(1), rules.get(0).getThu());
        assertEquals(List.of(6, 7), rules.get(1).getThu());
        assertEquals("05:00", rules.get(1).getTuGio());
        assertEquals("Cuối tuần", rules.get(1).getTen());
        assertEquals(130_000, rules.get(1).getGia());
        assertEquals(null, rules.get(0).getTen());
    }

    @Test
    void normalizeRejectsInvalidRules() {
        var overlap = assertThrows(ResponseStatusException.class, () -> PricingService.normalize(List.of(
                new KhungGia(new ArrayList<>(List.of(1, 2)), "17:00", "22:00", 150_000, null),
                new KhungGia(new ArrayList<>(List.of(2)), "21:00", "23:00", 160_000, null))));
        assertTrue(overlap.getReason().contains("trùng"));
        // Khác ngày thì không tính là trùng.
        assertEquals(2, PricingService.normalize(List.of(
                new KhungGia(new ArrayList<>(List.of(1)), "17:00", "22:00", 150_000, null),
                new KhungGia(new ArrayList<>(List.of(2)), "17:00", "22:00", 150_000, null))).size());
        assertThrows(ResponseStatusException.class, () -> PricingService.normalize(List.of(new KhungGia(new ArrayList<>(List.of(1)), "17:15", "22:00", 1, null))));
        assertThrows(ResponseStatusException.class, () -> PricingService.normalize(List.of(new KhungGia(new ArrayList<>(List.of(1)), "22:00", "17:00", 1, null))));
        assertThrows(ResponseStatusException.class, () -> PricingService.normalize(List.of(new KhungGia(new ArrayList<>(), "17:00", "22:00", 1, null))));
        assertThrows(ResponseStatusException.class, () -> PricingService.normalize(List.of(new KhungGia(new ArrayList<>(List.of(8)), "17:00", "22:00", 1, null))));
        assertThrows(ResponseStatusException.class, () -> PricingService.normalize(List.of(new KhungGia(new ArrayList<>(List.of(1)), "17:00", "24:30", 1, null))));
        assertThrows(ResponseStatusException.class, () -> PricingService.normalize(List.of(new KhungGia(new ArrayList<>(List.of(1)), "17:00", "22:00", -5, null))));
    }
}
