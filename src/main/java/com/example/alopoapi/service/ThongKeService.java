package com.example.alopoapi.service;

import com.example.alopoapi.dto.TopCumSanDTO;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThongKeService {

    @Autowired
    private PhieuDatSanRepo phieuDatSanRepository;

    public List<TopCumSanDTO> getTop10CumSanDatNhieuNhat() {
        Pageable pageable = PageRequest.of(0, 10);
        return phieuDatSanRepository.findTopCumSan(pageable);
    }
}