package com.example.alopoapi.service;

import com.example.alopoapi.entity.voucher.ChiTietKhuyenMaiEntity;
import com.example.alopoapi.entity.voucher.KhuyenMaiEntity;
import com.example.alopoapi.repository.ChiTietKhuyenMaiRepo;
import com.example.alopoapi.repository.KhuyenMaiRepo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KhuyenMaiService {

    private final KhuyenMaiRepo khuyenMaiRepo;
    private final ChiTietKhuyenMaiRepo chiTietKhuyenMaiRepo;
    private final MongoTemplate mongoTemplate;

    public KhuyenMaiService(
            KhuyenMaiRepo khuyenMaiRepo,
            ChiTietKhuyenMaiRepo chiTietKhuyenMaiRepo,
            MongoTemplate mongoTemplate
    ) {
        this.khuyenMaiRepo = khuyenMaiRepo;
        this.chiTietKhuyenMaiRepo = chiTietKhuyenMaiRepo;
        this.mongoTemplate = mongoTemplate;
    }

    /** Voucher user đang sở hữu, còn hiệu lực và chưa sử dụng. */
    public List<KhuyenMaiEntity> getKhuyenMaiCuaUser(String username) {
        Set<String> unusedIds = chiTietKhuyenMaiRepo.findByMaNguoiDung(username).stream()
                .filter(ct -> !ct.isDaSuDung())
                .map(ChiTietKhuyenMaiEntity::getMaKM)
                .collect(Collectors.toSet());
        if (unusedIds.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        return khuyenMaiRepo.findAllById(unusedIds).stream()
                .filter(km -> conHieuLuc(km, now))
                .toList();
    }

    public List<KhuyenMaiEntity> getKhuyenMaiChuaCo(String username) {
        Set<String> owned = chiTietKhuyenMaiRepo.findByMaNguoiDung(username).stream()
                .map(ChiTietKhuyenMaiEntity::getMaKM)
                .collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        return khuyenMaiRepo.findAll().stream()
                .filter(km -> !owned.contains(km.getMaKhuyenMai()))
                .filter(km -> conHieuLuc(km, now))
                .toList();
    }

    public String nhanKhuyenMai(String username, String maKhuyenMai) {
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            return "Không tìm thấy khuyến mãi";
        }
        if (chiTietKhuyenMaiRepo.existsByMaNguoiDungAndMaKM(username, maKhuyenMai)) {
            return "Bạn đã nhận khuyến mãi này";
        }

        KhuyenMaiEntity khuyenMai = khuyenMaiRepo.findById(maKhuyenMai).orElse(null);
        if (khuyenMai == null) {
            return "Không tìm thấy khuyến mãi";
        }
        if (!conHieuLuc(khuyenMai, LocalDateTime.now())) {
            return "Khuyến mãi đã hết hạn";
        }

        // _id cố định theo (user, mã KM) để 2 request đồng thời không tạo 2 bản ghi.
        ChiTietKhuyenMaiEntity ct = new ChiTietKhuyenMaiEntity(username + ":" + maKhuyenMai, maKhuyenMai, username);
        ct.setDaSuDung(false);
        try {
            mongoTemplate.insert(ct);
        } catch (DuplicateKeyException exception) {
            return "Bạn đã nhận khuyến mãi này";
        }
        return "Nhận khuyến mãi thành công";
    }

    private boolean conHieuLuc(KhuyenMaiEntity km, LocalDateTime now) {
        return km.getNgayBatDau() != null && km.getNgayKetThuc() != null
                && !now.isBefore(km.getNgayBatDau())
                && !now.isAfter(km.getNgayKetThuc());
    }
}
