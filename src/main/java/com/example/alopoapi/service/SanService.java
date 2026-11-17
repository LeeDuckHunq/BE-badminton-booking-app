package com.example.alopoapi.service;

import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.repository.SanRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanService {

    private final SanRepo sanRepo;

    public SanService(SanRepo sanRepo) {
        this.sanRepo = sanRepo;
    }

    /** Các sân đang hoạt động của một cụm sân. */
    public List<SanEntity> getSanTheoCum(String maCumSan) {
        return sanRepo.findByMaCumSan(maCumSan).stream()
                .filter(SanEntity::isTrangThai)
                .toList();
    }

    public int getSoLuongSan() {
        return (int) sanRepo.count();
    }

    public SanEntity getSanInfo(String maSan) {
        return sanRepo.findById(maSan).orElse(null);
    }
}
