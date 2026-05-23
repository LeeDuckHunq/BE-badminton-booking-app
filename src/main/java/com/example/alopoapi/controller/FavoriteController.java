package com.example.alopoapi.controller;

import com.example.alopoapi.entity.FavoriteEntity;
import com.example.alopoapi.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/yeu-thich")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/{maNguoiDung}")
    public ResponseEntity<List<FavoriteEntity>> getUserFavorites(
            @PathVariable String maNguoiDung) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(maNguoiDung));
    }

    @PostMapping("/them")
    public ResponseEntity<?> addFav(@RequestBody FavoriteEntity fav) {
        try {
            fav.setMaFav("FAV_" + UUID.randomUUID());
            favoriteService.addFavForUser(fav);
            return ResponseEntity.ok("Đã thêm vào yêu thích");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/xoa/{maFav}")
    public ResponseEntity<?> deleteFav(@PathVariable String maFav) {
        try {
            favoriteService.deleteFavFromUser(maFav);
            return ResponseEntity.ok("Đã xóa khỏi yêu thích");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/kiem-tra")
    public ResponseEntity<Boolean> kiemTra(
            @RequestParam String maNguoiDung,
            @RequestParam String maSan) {
        List<FavoriteEntity> favList = favoriteService.getUserFavorites(maNguoiDung);
        boolean isFav = favList.stream()
                .anyMatch(f -> f.getMaCumSan().equals(maSan));
        return ResponseEntity.ok(isFav);
    }
}
