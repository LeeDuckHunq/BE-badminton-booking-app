package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.SanRepo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CumSanService {

    private final CumSanRepo cumSanRepo;
    private final SanRepo sanRepo;

    public CumSanService(CumSanRepo cumSanRepo, SanRepo sanRepo) {
        this.cumSanRepo = cumSanRepo;
        this.sanRepo = sanRepo;
    }

    /** Tối đa {@code soLuong} cụm sân đang hoạt động, kèm giá thấp nhất và số sân. */
    public List<CumSanEntity> getCumSan(int soLuong) {
        if (soLuong <= 0) {
            return List.of();
        }
        List<CumSanEntity> venues = cumSanRepo.findAll().stream()
                .filter(cs -> "ACTIVE".equals(cs.getTrangThai()))
                .limit(soLuong)
                .toList();
        attachCourtSummary(venues, sanRepo.findAll());
        return venues;
    }

    public CumSanEntity getCumSanInfo(String maCumSan) {
        CumSanEntity venue = cumSanRepo.findById(maCumSan).orElse(null);
        if (venue != null) {
            attachCourtSummary(List.of(venue), sanRepo.findByMaCumSan(maCumSan));
        }
        return venue;
    }

    private void attachCourtSummary(Collection<CumSanEntity> venues, List<SanEntity> courts) {
        Map<String, DoubleSummaryStatistics> stats = courts.stream()
                .filter(SanEntity::isTrangThai)
                .filter(court -> court.getMaCumSan() != null)
                .collect(Collectors.groupingBy(SanEntity::getMaCumSan, Collectors.summarizingDouble(SanEntity::getGia)));
        for (CumSanEntity venue : venues) {
            DoubleSummaryStatistics s = stats.get(venue.getMaCumSan());
            venue.setSoSan(s == null ? 0 : (int) s.getCount());
            venue.setGiaTu(s == null ? null : s.getMin());
        }
    }
}
