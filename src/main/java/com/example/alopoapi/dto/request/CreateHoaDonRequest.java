package com.example.alopoapi.dto.request;

/**
 * Dữ liệu client được phép gửi khi tạo hoá đơn.
 * Các trường như tongTien, ngayLap, maHoaDon (nếu client gửi) sẽ bị bỏ qua; server tự tính.
 */
public class CreateHoaDonRequest {

    private String maPhieuDat;
    private String maKhuyenMai;
    private String billThanhToan;

    public String getMaPhieuDat() {
        return maPhieuDat;
    }

    public void setMaPhieuDat(String maPhieuDat) {
        this.maPhieuDat = maPhieuDat;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getBillThanhToan() {
        return billThanhToan;
    }

    public void setBillThanhToan(String billThanhToan) {
        this.billThanhToan = billThanhToan;
    }
}
