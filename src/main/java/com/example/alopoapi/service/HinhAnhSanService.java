package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.repository.HinhAnhSanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HinhAnhSanService {

    @Autowired
    private HinhAnhSanRepo hinhAnhSanRepo;

    public List<String> getHinhAnh(String _maCumSan) {

        List<String> hinhAnh = new ArrayList<>();

        for (var ha : hinhAnhSanRepo.findAll()) {
            if (ha.getMaCumSan().equals(_maCumSan)) {
                hinhAnh.add(ha.getDuongDanHinhAnh());
            }
        }

        return hinhAnh;
    }

    public Map<String, String> getAllAnhSan() {

        Map<String, String> hinhAnhList = new HashMap<>();

        for (var ha : hinhAnhSanRepo.findAll()) {
            hinhAnhList.put(ha.getMaCumSan(), ha.getDuongDanHinhAnh());
        }

        return hinhAnhList;
    }
}
