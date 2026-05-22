package com.example.alopoapi.service;

import com.example.alopoapi.entity.HoaDonEntity;
import com.example.alopoapi.repository.HoaDonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonRepo hoaDonRepo;

    public boolean addHoaDon(HoaDonEntity hoaDon) {
        System.out.println("Before save: "
                + hoaDon.getNgayLap());

        hoaDonRepo.save(hoaDon);

        System.out.println("After save: "
                + hoaDon.getNgayLap());

        return true;
    }

    public HoaDonEntity getHoaDon(String maPhieuDat) {

        var exist = hoaDonRepo.findById(maPhieuDat);

        return exist.get();
    }
}
