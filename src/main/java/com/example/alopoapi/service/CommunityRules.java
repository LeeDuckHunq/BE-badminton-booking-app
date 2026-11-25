package com.example.alopoapi.service;

import com.example.alopoapi.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/** Quy tắc và tiện ích dùng chung cho hội nhóm và ghép trận. */
final class CommunityRules {

    private CommunityRules() {
    }

    /** Trình độ từ thấp đến cao: Yếu, TB-, TB, TB+, Khá, Giỏi. */
    static final List<String> LEVELS = List.of("YEU", "TB_YEU", "TB", "TB_KHA", "KHA", "GIOI");

    static final List<String> PLAY_TYPES = List.of("DOI", "DON", "GIAO_LUU");

    /** Mã ngân hàng hỗ trợ VietQR. */
    static final List<String> BANKS = List.of("MB", "VCB", "TCB", "ACB", "BIDV", "ICB", "VPB", "TPB", "VIB", "STB", "VBA", "HDB", "MSB", "SHB", "OCB");

    /** Người gửi tin nhắn hệ thống trong chat nhóm (username thật không chứa '#'). */
    static final String SYSTEM_SENDER = "#system";

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    static ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    /** Chuỗi đã trim; rỗng → null; dài hơn max → lỗi. */
    static String text(Object value, int max, String label) {
        if (value == null) return null;
        String s = value.toString().trim().replaceAll("\\s+\\n", "\n");
        if (s.isEmpty()) return null;
        if (s.length() > max) throw bad(label + " tối đa " + max + " ký tự");
        return s;
    }

    static String required(Object value, int min, int max, String label) {
        String s = text(value, max, label);
        if (s == null || s.length() < min) throw bad(label + " phải có ít nhất " + min + " ký tự");
        return s;
    }

    /** Trình độ hợp lệ hoặc null (mọi trình độ). */
    static String level(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        String s = value.toString().trim().toUpperCase();
        if (!LEVELS.contains(s)) throw bad("Trình độ không hợp lệ");
        return s;
    }

    static int levelIndex(String level) {
        return level == null ? -1 : LEVELS.indexOf(level);
    }

    static Integer integer(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                throw bad("Giá trị số không hợp lệ");
            }
        }
        return null;
    }

    static Long number(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                throw bad("Giá trị số không hợp lệ");
            }
        }
        return null;
    }

    static boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return fallback;
    }

    static Object get(Map<String, Object> body, String key) {
        return body == null ? null : body.get(key);
    }

    static String inviteCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        return sb.toString();
    }

    static String fullName(UserEntity user, String fallback) {
        return user != null && user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName().trim() : fallback;
    }

    /** Ảnh bill: chỉ chấp nhận link https (ảnh tải lên Supabase). */
    static String billUrl(Object value) {
        String url = text(value, 500, "Link ảnh");
        if (url != null && !url.startsWith("https://")) throw bad("Link ảnh không hợp lệ");
        return url;
    }
}
