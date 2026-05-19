package com.example.alopoapi.service;

import com.example.alopoapi.entity.KhuyenMaiEntity;
import com.example.alopoapi.repository.ChiTietKhuyenMaiRepo;
import com.example.alopoapi.repository.KhuyenMaiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class KhuyenMaiService {

    @Autowired
    private KhuyenMaiRepo khuyenMaiRepo;

    @Autowired
    private ChiTietKhuyenMaiRepo chiTietKhuyenMaiRepo;

    public List<KhuyenMaiEntity> getKhuyenMaiCuaUser(String username) {

        List<KhuyenMaiEntity> kmList = new ArrayList<>();

        for (var _km : chiTietKhuyenMaiRepo.findAll()) {
            if (_km.getMaNguoiDung().equals(username)) {
                for (var km : khuyenMaiRepo.findAll()) {
                    if (_km.getMaKM().equals(km.getMaKhuyenMai())) {
                        if (LocalDateTime.now().isBefore(km.getNgayKetThuc()) &&
                                LocalDateTime.now().isAfter(km.getNgayBatDau())) {
                            kmList.add(km);
                            break;
                        }
                    }
                }
            }
        }

        return kmList;
    }
}
