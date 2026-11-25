package com.example.alopoapi.service;

import com.example.alopoapi.entity.PhienBanAppEntity;
import com.example.alopoapi.repository.PhienBanAppRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Phiên bản app để app tự kiểm tra và cập nhật. */
@Service
public class PhienBanAppService {

    static final Set<String> PLATFORMS = Set.of("android");
    static final Set<String> ABIS = Set.of("arm64-v8a", "armeabi-v7a", "x86_64", "universal");

    private final PhienBanAppRepo repo;

    public PhienBanAppService(PhienBanAppRepo repo) {
        this.repo = repo;
    }

    /**
     * Thông tin bản mới nhất. Nếu truyền {@code build} (build của app đang chạy) thì kèm
     * {@code coBanMoi} và {@code batBuoc} (build hiện tại thấp hơn build tối thiểu).
     */
    public Map<String, Object> latest(String platform, Integer build) {
        String p = platform(platform);
        PhienBanAppEntity v = repo.findTopByNenTangOrderBySoBuildDesc(p).orElse(null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nenTang", p);
        if (v == null) {
            result.put("coBanMoi", false);
            result.put("batBuoc", false);
            return result;
        }
        result.putAll(view(v));
        if (build != null) {
            result.put("coBanMoi", build < v.getSoBuild());
            result.put("batBuoc", build < v.getBuildToiThieu());
        }
        return result;
    }

    public List<Map<String, Object>> history(String platform) {
        return repo.findByNenTangOrderBySoBuildDesc(platform(platform)).stream().map(this::view).toList();
    }

    /** Admin đăng (hoặc sửa) một bản phát hành. */
    public Map<String, Object> publish(Map<String, Object> body, String admin) {
        String p = platform(str(body.get("nenTang"), "android"));
        String name = str(body.get("phienBan"), null);
        if (name == null || !name.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) throw bad("Tên phiên bản dạng 1.2.3");
        int build = integer(body.get("soBuild"), "Số build");
        if (build < 1 || build > 999) throw bad("Số build từ 1 đến 999");
        int min = body.get("buildToiThieu") == null ? 0 : integer(body.get("buildToiThieu"), "Build tối thiểu");
        if (min < 0 || min > build) throw bad("Build tối thiểu phải từ 0 đến số build của bản này");

        List<String> changes = new ArrayList<>();
        if (body.get("thayDoi") instanceof List<?> list) {
            for (Object o : list) {
                String line = o == null ? "" : o.toString().trim();
                if (line.isEmpty()) continue;
                if (line.length() > 200) throw bad("Mỗi dòng thay đổi tối đa 200 ký tự");
                changes.add(line);
            }
        }
        if (changes.size() > 20) throw bad("Tối đa 20 dòng thay đổi");

        Map<String, String> links = new LinkedHashMap<>();
        Map<String, Long> sizes = new LinkedHashMap<>();
        if (body.get("taiVe") instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String abi = String.valueOf(e.getKey());
                String url = e.getValue() == null ? "" : e.getValue().toString().trim();
                if (url.isEmpty()) continue;
                if (!ABIS.contains(abi)) throw bad("Kiến trúc không hợp lệ: " + abi);
                if (!url.startsWith("https://") || url.length() > 500) throw bad("Link tải phải là https://");
                links.put(abi, url);
            }
        }
        if (links.isEmpty()) throw bad("Cần ít nhất một link tải APK");
        if (body.get("kichThuoc") instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getValue() instanceof Number n && links.containsKey(String.valueOf(e.getKey()))) sizes.put(String.valueOf(e.getKey()), n.longValue());
            }
        }

        String id = p + "-" + build;
        PhienBanAppEntity v = repo.findById(id).orElseGet(PhienBanAppEntity::new);
        v.setId(id);
        v.setNenTang(p);
        v.setPhienBan(name);
        v.setSoBuild(build);
        v.setBuildToiThieu(min);
        v.setThayDoi(changes);
        v.setTaiVe(links);
        v.setKichThuoc(sizes);
        if (v.getNgayPhatHanh() == null) v.setNgayPhatHanh(LocalDateTime.now());
        v.setNguoiDang(admin);
        return view(repo.save(v));
    }

    public void delete(String id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên bản");
        repo.deleteById(id);
    }

    private Map<String, Object> view(PhienBanAppEntity v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("nenTang", v.getNenTang());
        m.put("phienBan", v.getPhienBan());
        m.put("soBuild", v.getSoBuild());
        m.put("buildToiThieu", v.getBuildToiThieu());
        m.put("thayDoi", v.getThayDoi());
        m.put("taiVe", v.getTaiVe());
        m.put("kichThuoc", v.getKichThuoc());
        m.put("ngayPhatHanh", v.getNgayPhatHanh());
        return m;
    }

    private static String platform(String p) {
        String v = p == null || p.isBlank() ? "android" : p.trim().toLowerCase();
        if (!PLATFORMS.contains(v)) throw bad("Nền tảng không hỗ trợ");
        return v;
    }

    private static String str(Object o, String fallback) {
        return o == null || o.toString().isBlank() ? fallback : o.toString().trim();
    }

    private static int integer(Object o, String label) {
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) {
            throw bad(label + " không hợp lệ");
        }
    }

    private static ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }
}
