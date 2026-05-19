package com.example.alopoapi.service;

import com.example.alopoapi.entity.HoaDonEntity;
import com.example.alopoapi.repository.HoaDonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonRepo hoaDonRepo;

    public boolean addHoaDon(HoaDonEntity hoadon) {
        hoaDonRepo.save(hoadon);

        return true;
    }
}
