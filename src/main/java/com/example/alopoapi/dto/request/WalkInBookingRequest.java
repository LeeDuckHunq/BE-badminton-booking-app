package com.example.alopoapi.dto.request;

import java.time.LocalDateTime;

/** Chủ sân / admin đặt hộ khách (gọi điện hoặc tới sân). */
public class WalkInBookingRequest {

    private String maSan;
    private LocalDateTime batDau;
    private LocalDateTime ketThuc;
    /** Username khách nếu khách có tài khoản SmashZone (tuỳ chọn). */
    private String maNguoiDung;
    /** Bắt buộc với khách vãng lai. */
    private String tenKhachHang;
    private String sdtKhachHang;
    /** TIEN_MAT, CHUYEN_KHOAN hoặc TRA_SAU. */
    private String phuongThucThanhToan;
    private String ghiChu;

    public String getMaSan() { return maSan; }
    public void setMaSan(String maSan) { this.maSan = maSan; }
    public LocalDateTime getBatDau() { return batDau; }
    public void setBatDau(LocalDateTime batDau) { this.batDau = batDau; }
    public LocalDateTime getKetThuc() { return ketThuc; }
    public void setKetThuc(LocalDateTime ketThuc) { this.ketThuc = ketThuc; }
    public String getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(String maNguoiDung) { this.maNguoiDung = maNguoiDung; }
    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
    public String getSdtKhachHang() { return sdtKhachHang; }
    public void setSdtKhachHang(String sdtKhachHang) { this.sdtKhachHang = sdtKhachHang; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
