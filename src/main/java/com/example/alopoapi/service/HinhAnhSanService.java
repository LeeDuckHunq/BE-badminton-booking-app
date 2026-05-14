package com.example.alopoapi.service;

import com.example.alopoapi.repository.HinhAnhSanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
