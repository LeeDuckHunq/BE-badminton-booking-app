package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.repository.CumSanRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CumSanService {

    private final CumSanRepo cumSanRepo;

    public CumSanService(CumSanRepo cumSanRepo) {
        this.cumSanRepo = cumSanRepo;
    }

    /** Tối đa {@code soLuong} cụm sân đang hoạt động. */
    public List<CumSanEntity> getCumSan(int soLuong) {
        if (soLuong <= 0) {
            return List.of();
        }
        return cumSanRepo.findAll().stream()
                .filter(cs -> "ACTIVE".equals(cs.getTrangThai()))
                .limit(soLuong)
                .toList();
    }

    public CumSanEntity getCumSanInfo(String maCumSan) {
        return cumSanRepo.findById(maCumSan).orElse(null);
    }
}
