package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.repository.CumSanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CumSanService {

    @Autowired
    private CumSanRepo cumSanRepo;

    public List<CumSanEntity> getCumSan(int soLuong) {

        if (soLuong > cumSanRepo.count()) soLuong = (int)cumSanRepo.count();

        List<CumSanEntity> cumSanList = new ArrayList<>();
        List<CumSanEntity> ds = cumSanRepo.findAll();

        for (int i = 0; i < soLuong; i++) {
            cumSanList.add(ds.get(i));
        }

        return cumSanList;
    }
}
