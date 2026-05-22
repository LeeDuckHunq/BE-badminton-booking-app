package com.example.alopoapi.service;

import com.example.alopoapi.entity.voucher.ChiTietKhuyenMaiEntity;
import com.example.alopoapi.entity.voucher.KhuyenMaiEntity;
import com.example.alopoapi.repository.ChiTietKhuyenMaiRepo;
import com.example.alopoapi.repository.KhuyenMaiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class KhuyenMaiService {

    @Autowired
    private KhuyenMaiRepo khuyenMaiRepo;

    @Autowired
    private ChiTietKhuyenMaiRepo chiTietKhuyenMaiRepo;

    public List<KhuyenMaiEntity> getKhuyenMaiCuaUser(String username) {

        List<KhuyenMaiEntity> kmList = new ArrayList<>();

        for (var _km : chiTietKhuyenMaiRepo.findAll()) {
            if (_km.getMaNguoiDung().equals(username)) {
                for (var km : khuyenMaiRepo.findAll()) {
                    if (_km.getMaKM().equals(km.getMaKhuyenMai())) {
                        if (LocalDateTime.now().isBefore(km.getNgayKetThuc()) &&
                                LocalDateTime.now().isAfter(km.getNgayBatDau())) {
                            kmList.add(km);
                            break;
                        }
                    }
                }
            }
        }

        return kmList;
    }

    public List<KhuyenMaiEntity> getKhuyenMaiChuaCo(String username) {

        List<KhuyenMaiEntity> kmList = new ArrayList<>();

        List<String> maKMUserDaCo = new ArrayList<>();

        for (var ctkm : chiTietKhuyenMaiRepo.findAll()) {
            if (ctkm.getMaNguoiDung().equals(username)) {
                maKMUserDaCo.add(ctkm.getMaKM());
            }
        }

        for (var km : khuyenMaiRepo.findAll()) {

            boolean chuaCo = !maKMUserDaCo.contains(km.getMaKhuyenMai());

            boolean conHieuLuc =
                    (LocalDateTime.now().isAfter(km.getNgayBatDau()) ||
                            LocalDateTime.now().isEqual(km.getNgayBatDau()))
                            &&
                            (LocalDateTime.now().isBefore(km.getNgayKetThuc()) ||
                                    LocalDateTime.now().isEqual(km.getNgayKetThuc()));

            if (chuaCo && conHieuLuc) {
                kmList.add(km);
            }
        }

        return kmList;
    }

    public String nhanKhuyenMai(String username, String maKhuyenMai) {

        for (var ct : chiTietKhuyenMaiRepo.findAll()) {

            if (ct.getMaNguoiDung().equals(username) && ct.getMaKM().equals(maKhuyenMai)) {

                return "Bạn đã nhận khuyến mãi này";
            }
        }

        KhuyenMaiEntity khuyenMai = null;

        for (var km : khuyenMaiRepo.findAll()) {

            if (km.getMaKhuyenMai().equals(maKhuyenMai)) {
                khuyenMai = km;
                break;
            }
        }

        if (khuyenMai == null) {
            return "Không tìm thấy khuyến mãi";
        }

        LocalDateTime now = LocalDateTime.now();

        boolean conHieuLuc =
                !now.isBefore(khuyenMai.getNgayBatDau())
                        && !now.isAfter(khuyenMai.getNgayKetThuc());

        if (!conHieuLuc) {
            return "Khuyến mãi đã hết hạn";
        }

        ChiTietKhuyenMaiEntity ct = new ChiTietKhuyenMaiEntity();

        ct.setMaNguoiDung(username);
        ct.setMaKM(maKhuyenMai);

        chiTietKhuyenMaiRepo.save(ct);

        return "Nhận khuyến mãi thành công";
    }
}
