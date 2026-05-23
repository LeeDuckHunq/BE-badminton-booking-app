package com.example.alopoapi.service;

import com.example.alopoapi.entity.FavoriteEntity;
import com.example.alopoapi.repository.FavoriteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepo favoriteRepo;

    public void addFavForUser(FavoriteEntity fav) {
        if (favoriteRepo.existsByMaNguoiDungAndMaSan(fav.getMaNguoiDung(), fav.getMaCumSan())) {
            throw new RuntimeException("Sân này đã có trong yêu thích");
        }
        favoriteRepo.save(fav);
    }

    public void deleteFavFromUser(String maFav) {
        if (!favoriteRepo.existsById(maFav)) {
            throw new RuntimeException("Không tìm thấy mục yêu thích");
        }
        favoriteRepo.deleteById(maFav);
    }

    public List<FavoriteEntity> getUserFavorites(String maNguoiDung) {
        return favoriteRepo.findAllByMaNguoiDung(maNguoiDung);
    }
}
