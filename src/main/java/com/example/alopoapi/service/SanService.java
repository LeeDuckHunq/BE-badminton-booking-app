package com.example.alopoapi.service;

import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.repository.SanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SanService {

    @Autowired
    private SanRepo sanRepo;

    public List<SanEntity> getSanTheoCum(String maCum) {

        List<SanEntity> sanList = new ArrayList<>();

        for (var s : sanRepo.findAll()) {
            if (s.getMaCumSan().equals(maCum)) {
                sanList.add(s);
            }
        }

        return sanList;
    }

    public int getSoLuongSan() {

        return (int)sanRepo.count();

    }

    public SanEntity getSanInfo(String maSan) {

        for (var s : sanRepo.findAll()) {
            if (s.getMaSan().equals(maSan)) {
                return s;
            }
        }
        return null;
    }
}
