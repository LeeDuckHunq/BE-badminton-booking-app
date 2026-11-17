package com.example.alopoapi.service;

import com.example.alopoapi.entity.FavoriteEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.FavoriteRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteService {

    private final FavoriteRepo favoriteRepo;
    private final CumSanRepo cumSanRepo;

    public FavoriteService(FavoriteRepo favoriteRepo, CumSanRepo cumSanRepo) {
        this.favoriteRepo = favoriteRepo;
        this.cumSanRepo = cumSanRepo;
    }

    public FavoriteEntity addFavForUser(String username, String maCumSan) {
        if (maCumSan == null || maCumSan.isBlank() || !cumSanRepo.existsById(maCumSan)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy cụm sân");
        }
        if (favoriteRepo.existsByMaNguoiDungAndMaCumSan(username, maCumSan)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sân này đã có trong yêu thích");
        }
        return favoriteRepo.save(new FavoriteEntity("FAV_" + UUID.randomUUID(), username, maCumSan));
    }

    public void deleteFavFromUser(String maFav, String username) {
        FavoriteEntity fav = favoriteRepo.findById(maFav)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mục yêu thích"));
        if (fav.getMaNguoiDung() == null || !fav.getMaNguoiDung().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xoá mục yêu thích này");
        }
        favoriteRepo.deleteById(maFav);
    }

    public List<FavoriteEntity> getUserFavorites(String maNguoiDung) {
        return favoriteRepo.findAllByMaNguoiDung(maNguoiDung);
    }

    public boolean isFavorite(String maNguoiDung, String maCumSan) {
        return favoriteRepo.existsByMaNguoiDungAndMaCumSan(maNguoiDung, maCumSan);
    }
}
