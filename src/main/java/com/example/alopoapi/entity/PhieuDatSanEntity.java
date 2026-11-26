package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "PhieuDatSan")
public class PhieuDatSanEntity {

    @Id
    private String maPhieuDat;

    @Field("maNguoiDung")
    private String maNguoiDung;

    @Field("maSan")
    private String maSan;

    @Field("batDau")
    private LocalDateTime batDau;

    @Field("ketThuc")
    private LocalDateTime ketThuc;

    @Field("tongTien")
    private double tongTien;

    @Field("ngayLap")
    private LocalDateTime ngayLap;

    @Field("trangThai")
    private String trangThai;

    /** APP (khách tự đặt trên app) hoặc TAI_SAN (chủ sân đặt hộ). Null = APP (dữ liệu cũ). */
    @Field("nguonDat")
    private String nguonDat;

    /** Đơn đặt hộ: CHUYEN_KHOAN, TIEN_MAT hoặc TRA_SAU. Đơn từ app thanh toán qua hoá đơn. */
    @Field("phuongThucThanhToan")
    private String phuongThucThanhToan;

    /** Khách vãng lai (không có tài khoản) – hoặc tên/SĐT liên hệ do chủ sân ghi. */
    @Field("tenKhachHang")
    private String tenKhachHang;

    @Field("sdtKhachHang")
    private String sdtKhachHang;

    @Field("ghiChu")
    private String ghiChu;

    /** Tài khoản quản lý đã tạo đơn đặt hộ. */
    @Field("nguoiTao")
    private String nguoiTao;

    /** Yêu cầu huỷ của khách với đơn đã thanh toán / xác nhận: CHO_XU_LY, DONG_Y, TU_CHOI (null = chưa có). */
    @Field("yeuCauHuy")
    private String yeuCauHuy;

    @Field("lyDoHuy")
    private String lyDoHuy;

    @Field("ngayYeuCauHuy")
    private LocalDateTime ngayYeuCauHuy;

    /** Ghi chú của chủ sân khi đồng ý / từ chối yêu cầu huỷ. */
    @Field("phanHoiHuy")
    private String phanHoiHuy;

    /** Chủ sân chuyển lượt sang sân khác (sân cũ có sự cố). */
    @Field("maSanCu")
    private String maSanCu;

    @Field("lyDoDoiSan")
    private String lyDoDoiSan;

    @Field("ngayDoiSan")
    private LocalDateTime ngayDoiSan;

    //region constructor

    public PhieuDatSanEntity() {
    }

    public PhieuDatSanEntity(String maPhieuDat, String maNguoiDung, String maSan,
                             LocalDateTime batDau, LocalDateTime ketThuc, double tongTien,
                             LocalDateTime ngayLap, String trangThai) {
        this.maPhieuDat = maPhieuDat;
        this.maNguoiDung = maNguoiDung;
        this.maSan = maSan;
        this.batDau = batDau;
        this.ketThuc = ketThuc;
        this.tongTien = tongTien;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
    }
    //endregion

    //region getter - setter

    public String getMaPhieuDat() {
        return maPhieuDat;
    }

    public void setMaPhieuDat(String maPhieuDat) {
        this.maPhieuDat = maPhieuDat;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getMaSan() {
        return maSan;
    }

    public void setMaSan(String maSan) {
        this.maSan = maSan;
    }

    public LocalDateTime getBatDau() {
        return batDau;
    }

    public void setBatDau(LocalDateTime batDau) {
        this.batDau = batDau;
    }

    public LocalDateTime getKetThuc() {
        return ketThuc;
    }

    public void setKetThuc(LocalDateTime ketThuc) {
        this.ketThuc = ketThuc;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNguonDat() {
        return nguonDat;
    }

    public void setNguonDat(String nguonDat) {
        this.nguonDat = nguonDat;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSdtKhachHang() {
        return sdtKhachHang;
    }

    public void setSdtKhachHang(String sdtKhachHang) {
        this.sdtKhachHang = sdtKhachHang;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }
    public String getYeuCauHuy() { return yeuCauHuy; }
    public void setYeuCauHuy(String yeuCauHuy) { this.yeuCauHuy = yeuCauHuy; }
    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String lyDoHuy) { this.lyDoHuy = lyDoHuy; }
    public LocalDateTime getNgayYeuCauHuy() { return ngayYeuCauHuy; }
    public void setNgayYeuCauHuy(LocalDateTime ngayYeuCauHuy) { this.ngayYeuCauHuy = ngayYeuCauHuy; }
    public String getPhanHoiHuy() { return phanHoiHuy; }
    public void setPhanHoiHuy(String phanHoiHuy) { this.phanHoiHuy = phanHoiHuy; }
    public String getMaSanCu() { return maSanCu; }
    public void setMaSanCu(String maSanCu) { this.maSanCu = maSanCu; }
    public String getLyDoDoiSan() { return lyDoDoiSan; }
    public void setLyDoDoiSan(String lyDoDoiSan) { this.lyDoDoiSan = lyDoDoiSan; }
    public LocalDateTime getNgayDoiSan() { return ngayDoiSan; }
    public void setNgayDoiSan(LocalDateTime ngayDoiSan) { this.ngayDoiSan = ngayDoiSan; }
    //endregion

}
