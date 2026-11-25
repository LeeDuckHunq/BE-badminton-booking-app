package com.example.alopoapi.service;

import com.example.alopoapi.entity.KhungGia;
import com.example.alopoapi.entity.SanEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Tính giá sân theo khung giờ. Mỗi phút của lượt đặt được tính theo khung giá khớp
 * (thứ trong tuần + giờ), không khớp khung nào thì theo giá mặc định của sân.
 */
public final class PricingService {

    static final int MAX_RULES = 30;

    private PricingService() {
    }

    /** "05:30" → 330; "24:00" → 1440. */
    static int minutes(String hhmm) {
        if (hhmm == null || !hhmm.matches("\\d{1,2}:\\d{2}")) throw bad("Giờ không hợp lệ: " + hhmm);
        String[] p = hhmm.split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        if (h > 24 || m > 59 || (h == 24 && m != 0)) throw bad("Giờ không hợp lệ: " + hhmm);
        return h * 60 + m;
    }

    static String hhmm(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    /** Giá mỗi giờ tại một thời điểm. */
    public static double hourlyRate(SanEntity court, LocalDateTime at) {
        int day = at.getDayOfWeek().getValue();
        int minute = at.getHour() * 60 + at.getMinute();
        for (KhungGia rule : court.getBangGia()) {
            if (rule.getThu() != null && rule.getThu().contains(day)
                    && minute >= minutes(rule.getTuGio()) && minute < minutes(rule.getDenGio())) {
                return rule.getGia();
            }
        }
        return court.getGia();
    }

    /** Tổng tiền lượt đặt [start, end), tính theo từng phút, làm tròn đến đồng. */
    public static double total(SanEntity court, LocalDateTime start, LocalDateTime end) {
        if (court.getBangGia().isEmpty()) {
            long minutes = java.time.Duration.between(start, end).toMinutes();
            return Math.round(court.getGia() * minutes / 60.0);
        }
        double sum = 0;
        for (LocalDateTime t = start; t.isBefore(end); t = t.plusMinutes(1)) {
            sum += hourlyRate(court, t) / 60.0;
        }
        return Math.round(sum);
    }

    /** Giá thấp nhất sân có thể có (để hiển thị "giá từ"). */
    public static double minRate(SanEntity court) {
        double min = court.getGia();
        for (KhungGia rule : court.getBangGia()) min = Math.min(min, rule.getGia());
        return min;
    }

    /**
     * Kiểm tra và chuẩn hoá bảng giá: giờ theo bước 30 phút, từ &lt; đến, giá ≥ 0,
     * thứ trong 1..7, các khung không chồng nhau trong cùng một ngày.
     */
    public static List<KhungGia> normalize(List<KhungGia> rules) {
        if (rules == null) return new ArrayList<>();
        if (rules.size() > MAX_RULES) throw bad("Tối đa " + MAX_RULES + " khung giá mỗi sân");
        List<KhungGia> result = new ArrayList<>();
        for (KhungGia r : rules) {
            if (r == null) continue;
            TreeSet<Integer> days = new TreeSet<>();
            if (r.getThu() != null) {
                for (Integer d : r.getThu()) {
                    if (d == null || d < 1 || d > 7) throw bad("Thứ áp dụng không hợp lệ");
                    days.add(d);
                }
            }
            if (days.isEmpty()) throw bad("Mỗi khung giá cần chọn ít nhất một ngày áp dụng");
            int from = minutes(r.getTuGio());
            int to = minutes(r.getDenGio());
            if (from % 30 != 0 || to % 30 != 0) throw bad("Giờ của khung giá phải theo bước 30 phút");
            if (from >= to) throw bad("Giờ bắt đầu của khung giá phải trước giờ kết thúc");
            if (r.getGia() < 0 || r.getGia() > 10_000_000) throw bad("Giá của khung giờ không hợp lệ");
            String name = r.getTen() == null || r.getTen().isBlank() ? null : r.getTen().trim();
            if (name != null && name.length() > 40) throw bad("Tên khung giá tối đa 40 ký tự");
            KhungGia clean = new KhungGia(new ArrayList<>(days), hhmm(from), hhmm(to), Math.round(r.getGia()), name);
            for (KhungGia other : result) {
                boolean sameDay = other.getThu().stream().anyMatch(days::contains);
                boolean overlap = from < minutes(other.getDenGio()) && minutes(other.getTuGio()) < to;
                if (sameDay && overlap) {
                    throw bad("Khung " + clean.getTuGio() + "–" + clean.getDenGio() + " bị trùng với khung "
                            + other.getTuGio() + "–" + other.getDenGio() + " trong cùng ngày");
                }
            }
            result.add(clean);
        }
        result.sort((a, b) -> {
            int c = Integer.compare(a.getThu().get(0), b.getThu().get(0));
            return c != 0 ? c : Integer.compare(minutes(a.getTuGio()), minutes(b.getTuGio()));
        });
        return result;
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
