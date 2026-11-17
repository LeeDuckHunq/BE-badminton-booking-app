package com.example.alopoapi.controller;

import com.example.alopoapi.entity.FavoriteEntity;
import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/yeu-thich")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/{maNguoiDung}")
    public ResponseEntity<List<FavoriteEntity>> getUserFavorites(
            @PathVariable String maNguoiDung, Authentication authentication) {
        AccessGuard.requireSelfOrAdmin(authentication, maNguoiDung);
        return ResponseEntity.ok(favoriteService.getUserFavorites(maNguoiDung));
    }

    /** Luôn thêm cho tài khoản đang đăng nhập; trường maNguoiDung trong body bị bỏ qua. */
    @PostMapping("/them")
    public ResponseEntity<?> addFav(@RequestBody FavoriteEntity fav, Authentication authentication) {
        favoriteService.addFavForUser(AccessGuard.username(authentication), fav == null ? null : fav.getMaCumSan());
        return ResponseEntity.ok("Đã thêm vào yêu thích");
    }

    @DeleteMapping("/xoa/{maFav}")
    public ResponseEntity<?> deleteFav(@PathVariable String maFav, Authentication authentication) {
        favoriteService.deleteFavFromUser(maFav, AccessGuard.username(authentication));
        return ResponseEntity.ok("Đã xóa khỏi yêu thích");
    }

    @GetMapping("/kiem-tra")
    public ResponseEntity<Boolean> kiemTra(
            @RequestParam String maNguoiDung,
            @RequestParam String maCumSan,
            Authentication authentication) {
        AccessGuard.requireSelfOrAdmin(authentication, maNguoiDung);
        return ResponseEntity.ok(favoriteService.isFavorite(maNguoiDung, maCumSan));
    }
}
