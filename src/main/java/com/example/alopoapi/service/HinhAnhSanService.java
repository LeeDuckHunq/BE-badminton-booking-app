package com.example.alopoapi.service;

import com.example.alopoapi.entity.HinhAnhSanEntity;
import com.example.alopoapi.repository.HinhAnhSanRepo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HinhAnhSanService {

    private final HinhAnhSanRepo hinhAnhSanRepo;

    public HinhAnhSanService(HinhAnhSanRepo hinhAnhSanRepo) {
        this.hinhAnhSanRepo = hinhAnhSanRepo;
    }

    public List<String> getHinhAnh(String maCumSan) {
        return hinhAnhSanRepo.findByMaCumSan(maCumSan).stream()
                .map(HinhAnhSanEntity::getDuongDanHinhAnh)
                .toList();
    }

    /** Ảnh đại diện (ảnh đầu tiên) của mỗi cụm sân. */
    public Map<String, String> getAllAnhSan() {
        Map<String, String> result = new HashMap<>();
        for (var ha : hinhAnhSanRepo.findAll()) {
            if (ha.getMaCumSan() != null && ha.getDuongDanHinhAnh() != null) {
                result.putIfAbsent(ha.getMaCumSan(), ha.getDuongDanHinhAnh());
            }
        }
        return result;
    }
}
