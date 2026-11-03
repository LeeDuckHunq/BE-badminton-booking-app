package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "HoaDon")
public class HoaDonEntity {

    @Id
    private String maHoaDon;

    @Field("maPhieuDat")
    private String maPhieuDat;

    @Field("maKhuyenMai")
    private String maKhuyenMai;

    @Field("tongTien")
    private double tongTien;

    @Field("ngayLap")
    private LocalDateTime ngayLap;

    @Field("billThanhToan")
    private String billThanhToan;

    //region constructor

    public HoaDonEntity() {
    }

    public HoaDonEntity(String maHoaDon, String maPhieuDat, String maKhuyenMai,
                        double tongTien, LocalDateTime ngayLap, String billThanhToan) {
        this.maHoaDon = maHoaDon;
        this.maPhieuDat = maPhieuDat;
        this.maKhuyenMai = maKhuyenMai;
        this.tongTien = tongTien;
        this.ngayLap = ngayLap;
        this.billThanhToan = billThanhToan;
    }
    //endregion

    //region getter - setter

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

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

    public String getBillThanhToan() {
        return billThanhToan;
    }

    public void setBillThanhToan(String billThanhToan) {
        this.billThanhToan = billThanhToan;
    }

    //endregion
}
