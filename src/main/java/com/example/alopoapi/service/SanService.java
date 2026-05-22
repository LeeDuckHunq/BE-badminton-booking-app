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
                if (s.isTrangThai()) {
                    sanList.add(s);
                }
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

    public List<SanEntity> getAllSanTheoCum(String maCumSan) {

        List<SanEntity> sanList = new ArrayList<>();

        for (var s : sanRepo.findAll()) {
            if (s.getMaCumSan().equals(maCumSan)) {
                sanList.add(s);
            }
        }

        return sanList;
    }

    public boolean addNewSan(SanEntity san) {

        var exist = sanRepo.findById(san.getMaSan());

        if (exist.isEmpty()) {
            sanRepo.save(san);
            return true;
        }

        return false;
    }

    public boolean editSan(SanEntity san) {

        var exist = sanRepo.findById(san.getMaSan());

        if (exist.isPresent()) {
            sanRepo.save(san);
            return true;
        }

        return false;
    }

    public boolean deleteSan(String maSan) {

        var exist = sanRepo.findById(maSan);

        if (exist.isPresent()) {
            exist.get().setTrangThai(false);
            sanRepo.save(exist.get());
            return true;
        }

        return false;
    }
}
