package com.example.alopoapi.service;

import com.example.alopoapi.entity.PhienBanAppEntity;
import com.example.alopoapi.repository.PhienBanAppRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhienBanAppServiceTests {

    @Mock private PhienBanAppRepo repo;
    private PhienBanAppService service;
    private final Map<String, PhienBanAppEntity> store = new HashMap<>();

    @BeforeEach
    void setUp() {
        service = new PhienBanAppService(repo);
        when(repo.save(any())).thenAnswer(i -> { PhienBanAppEntity v = i.getArgument(0); store.put(v.getId(), v); return v; });
        when(repo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(store.get(i.<String>getArgument(0))));
        when(repo.findTopByNenTangOrderBySoBuildDesc(anyString())).thenAnswer(i -> store.values().stream().max(Comparator.comparingInt(PhienBanAppEntity::getSoBuild)));
    }

    private Map<String, Object> release(String name, int build, int min) {
        Map<String, Object> body = new HashMap<>();
        body.put("phienBan", name);
        body.put("soBuild", build);
        body.put("buildToiThieu", min);
        body.put("thayDoi", List.of("Ghép trận", "  ", "Giá theo khung giờ"));
        body.put("taiVe", Map.of("arm64-v8a", "https://example.com/a.apk"));
        body.put("kichThuoc", Map.of("arm64-v8a", 20_000_000, "x86_64", 5));
        return body;
    }

    @Test
    void noReleaseMeansNoUpdate() {
        Map<String, Object> r = service.latest("android", 1);
        assertEquals(false, r.get("coBanMoi"));
        assertEquals(false, r.get("batBuoc"));
    }

    @Test
    void reportsNewAndForcedUpdates() {
        service.publish(release("1.1.0", 2, 0), "admin");
        service.publish(release("1.2.0", 3, 2), "admin");

        Map<String, Object> fromOld = service.latest("android", 1);
        assertEquals("1.2.0", fromOld.get("phienBan"));
        assertEquals(true, fromOld.get("coBanMoi"));
        assertEquals(true, fromOld.get("batBuoc"));

        Map<String, Object> fromPrev = service.latest("android", 2);
        assertEquals(true, fromPrev.get("coBanMoi"));
        assertEquals(false, fromPrev.get("batBuoc"));

        assertEquals(false, service.latest("android", 3).get("coBanMoi"));
    }

    @Test
    void publishCleansChangelogAndSizes() {
        Map<String, Object> v = service.publish(release("1.1.0", 2, 0), "admin");
        assertEquals(List.of("Ghép trận", "Giá theo khung giờ"), v.get("thayDoi"));
        assertEquals(Map.of("arm64-v8a", 20_000_000L), v.get("kichThuoc"));
        assertEquals("android-2", v.get("id"));
    }

    @Test
    void rejectsInvalidReleases() {
        assertThrows(ResponseStatusException.class, () -> service.publish(release("1.1", 2, 0), "admin"));
        assertThrows(ResponseStatusException.class, () -> service.publish(release("1.1.0", 2, 3), "admin"));
        Map<String, Object> http = release("1.1.0", 2, 0);
        http.put("taiVe", Map.of("arm64-v8a", "http://insecure/a.apk"));
        assertThrows(ResponseStatusException.class, () -> service.publish(http, "admin"));
        Map<String, Object> noLink = release("1.1.0", 2, 0);
        noLink.put("taiVe", Map.of());
        assertThrows(ResponseStatusException.class, () -> service.publish(noLink, "admin"));
        assertThrows(ResponseStatusException.class, () -> service.latest("ios", 1));
    }
}
