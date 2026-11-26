package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.CreateHoaDonRequest;
import com.example.alopoapi.entity.HoaDonEntity;
import com.example.alopoapi.entity.PhieuDatSanEntity;
import com.example.alopoapi.entity.voucher.ChiTietKhuyenMaiEntity;
import com.example.alopoapi.entity.voucher.KhuyenMaiEntity;
import com.example.alopoapi.repository.HoaDonRepo;
import com.example.alopoapi.repository.KhuyenMaiRepo;
import com.example.alopoapi.repository.PhieuDatSanRepo;
import com.example.alopoapi.security.AccessGuard;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class HoaDonService {

    private final HoaDonRepo hoaDonRepo;
    private final PhieuDatSanRepo phieuDatSanRepo;
    private final KhuyenMaiRepo khuyenMaiRepo;
    private final StorageService storageService;
    private final ManagementService managementService;
    private final MongoTemplate mongoTemplate;

    public HoaDonService(
            HoaDonRepo hoaDonRepo,
            PhieuDatSanRepo phieuDatSanRepo,
            KhuyenMaiRepo khuyenMaiRepo,
            StorageService storageService,
            ManagementService managementService,
            MongoTemplate mongoTemplate
    ) {
        this.hoaDonRepo = hoaDonRepo;
        this.phieuDatSanRepo = phieuDatSanRepo;
        this.khuyenMaiRepo = khuyenMaiRepo;
        this.storageService = storageService;
        this.managementService = managementService;
        this.mongoTemplate = mongoTemplate;
    }

    private ThongBaoService thongBao;

    @Autowired(required = false)
    void setThongBaoService(ThongBaoService thongBao) {
        this.thongBao = thongBao;
    }

    /**
     * Tạo hoá đơn cho phiếu đặt của chính người dùng.
     * Tổng tiền luôn được tính lại ở server từ phiếu đặt + khuyến mãi; mọi giá trị tiền do client gửi đều bị bỏ qua.
     */
    public HoaDonEntity createHoaDon(CreateHoaDonRequest request, String username) {
        if (request == null || request.getMaPhieuDat() == null || request.getMaPhieuDat().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu mã phiếu đặt");
        }

        PhieuDatSanEntity booking = phieuDatSanRepo.findById(request.getMaPhieuDat())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu đặt"));
        if (booking.getMaNguoiDung() == null || !booking.getMaNguoiDung().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thanh toán phiếu đặt này");
        }
        if (!PhieuDatSanService.CHO_XAC_NHAN.equals(booking.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiếu đặt không còn ở trạng thái chờ thanh toán");
        }
        if (hoaDonRepo.existsByMaPhieuDat(booking.getMaPhieuDat())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiếu đặt này đã có hoá đơn");
        }

        String bill = request.getBillThanhToan();
        if (bill == null || !storageService.isOwnedPaymentProofUrl(bill, username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh bill thanh toán không hợp lệ");
        }

        String maKhuyenMai = request.getMaKhuyenMai() == null || request.getMaKhuyenMai().isBlank()
                ? null
                : request.getMaKhuyenMai().trim();
        String maHoaDon = "HD_" + booking.getMaPhieuDat();

        double phanTramGiam = 0;
        ChiTietKhuyenMaiEntity claimedVoucher = null;
        if (maKhuyenMai != null) {
            KhuyenMaiEntity khuyenMai = khuyenMaiRepo.findById(maKhuyenMai)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khuyến mãi không tồn tại"));
            LocalDateTime now = LocalDateTime.now();
            if (khuyenMai.getNgayBatDau() == null || khuyenMai.getNgayKetThuc() == null
                    || now.isBefore(khuyenMai.getNgayBatDau()) || now.isAfter(khuyenMai.getNgayKetThuc())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khuyến mãi đã hết hạn");
            }
            phanTramGiam = Math.max(0, Math.min(1, khuyenMai.getPhanTramGiam()));
            claimedVoucher = claimVoucher(username, maKhuyenMai, maHoaDon);
        }

        HoaDonEntity hoaDon = new HoaDonEntity(
                maHoaDon,
                booking.getMaPhieuDat(),
                maKhuyenMai,
                Math.round(booking.getTongTien() * (1 - phanTramGiam)),
                LocalDateTime.now(),
                bill
        );

        try {
            HoaDonEntity saved = mongoTemplate.insert(hoaDon);
            if (thongBao != null) {
                thongBao.invoiceSubmitted(booking, Math.round(saved.getTongTien()));
            }
            return saved;
        } catch (DuplicateKeyException exception) {
            if (claimedVoucher != null) {
                releaseVoucher(claimedVoucher.getId(), maHoaDon);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiếu đặt này đã có hoá đơn");
        }
    }

    public HoaDonEntity getHoaDon(String maPhieuDat, Authentication authentication) {
        PhieuDatSanEntity booking = phieuDatSanRepo.findById(maPhieuDat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu đặt"));

        boolean isCustomer = AccessGuard.isSelf(authentication, booking.getMaNguoiDung());
        if (!isCustomer && !managementService.canManageCourt(booking.getMaSan(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem hoá đơn này");
        }

        return hoaDonRepo.findFirstByMaPhieuDat(maPhieuDat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chưa có hoá đơn"));
    }

    /** Đánh dấu voucher đã dùng một cách nguyên tử để một voucher không thể dùng cho hai hoá đơn. */
    private ChiTietKhuyenMaiEntity claimVoucher(String username, String maKhuyenMai, String maHoaDon) {
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("maNguoiDung").is(username),
                Criteria.where("maKM").is(maKhuyenMai),
                Criteria.where("daSuDung").ne(true)
        ));
        Update update = new Update().set("daSuDung", true).set("maHoaDon", maHoaDon);
        ChiTietKhuyenMaiEntity claimed = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                ChiTietKhuyenMaiEntity.class
        );
        if (claimed == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn không sở hữu khuyến mãi này hoặc đã sử dụng");
        }
        return claimed;
    }

    private void releaseVoucher(String id, String maHoaDon) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id).and("maHoaDon").is(maHoaDon)),
                new Update().set("daSuDung", false).unset("maHoaDon"),
                ChiTietKhuyenMaiEntity.class
        );
    }
}
