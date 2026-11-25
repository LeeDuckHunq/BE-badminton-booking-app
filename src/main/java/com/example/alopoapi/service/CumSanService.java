package com.example.alopoapi.service;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.DanhGiaEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.repository.CumSanRepo;
import com.example.alopoapi.repository.DanhGiaRepo;
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
    private final DanhGiaRepo danhGiaRepo;

    public CumSanService(CumSanRepo cumSanRepo, SanRepo sanRepo, DanhGiaRepo danhGiaRepo) {
        this.cumSanRepo = cumSanRepo;
        this.sanRepo = sanRepo;
        this.danhGiaRepo = danhGiaRepo;
    }

    /** Tối đa {@code soLuong} cụm sân đang hoạt động, kèm giá thấp nhất, số sân và điểm đánh giá. */
    public List<CumSanEntity> getCumSan(int soLuong) {
        if (soLuong <= 0) {
            return List.of();
        }
        List<CumSanEntity> venues = cumSanRepo.findAll().stream()
                .filter(cs -> "ACTIVE".equals(cs.getTrangThai()))
                .limit(soLuong)
                .toList();
        attachCourtSummary(venues, sanRepo.findAll());
        attachRatings(venues, danhGiaRepo.findByAnFalse());
        return venues;
    }

    public CumSanEntity getCumSanInfo(String maCumSan) {
        CumSanEntity venue = cumSanRepo.findById(maCumSan).orElse(null);
        if (venue != null) {
            attachCourtSummary(List.of(venue), sanRepo.findByMaCumSan(maCumSan));
            attachRatings(List.of(venue), danhGiaRepo.findByMaCumSanAndAnFalseOrderByNgayTaoDesc(maCumSan));
        }
        return venue;
    }

    private void attachCourtSummary(Collection<CumSanEntity> venues, List<SanEntity> courts) {
        Map<String, DoubleSummaryStatistics> stats = courts.stream()
                .filter(SanEntity::isTrangThai)
                .filter(court -> court.getMaCumSan() != null)
                .collect(Collectors.groupingBy(SanEntity::getMaCumSan, Collectors.summarizingDouble(PricingService::minRate)));
        for (CumSanEntity venue : venues) {
            DoubleSummaryStatistics s = stats.get(venue.getMaCumSan());
            venue.setSoSan(s == null ? 0 : (int) s.getCount());
            venue.setGiaTu(s == null ? null : s.getMin());
        }
    }

    private void attachRatings(Collection<CumSanEntity> venues, List<DanhGiaEntity> visibleReviews) {
        Map<String, DoubleSummaryStatistics> stats = visibleReviews.stream()
                .filter(r -> r.getMaCumSan() != null)
                .collect(Collectors.groupingBy(DanhGiaEntity::getMaCumSan, Collectors.summarizingDouble(DanhGiaEntity::getSoSao)));
        for (CumSanEntity venue : venues) {
            DoubleSummaryStatistics s = stats.get(venue.getMaCumSan());
            venue.setSoDanhGia(s == null ? 0 : (int) s.getCount());
            venue.setDiemDanhGia(s == null ? null : Math.round(s.getAverage() * 10) / 10.0);
        }
    }
}
