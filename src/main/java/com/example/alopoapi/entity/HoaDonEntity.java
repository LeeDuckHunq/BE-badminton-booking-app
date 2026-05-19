package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "HoaDon")
public class HoaDonEntity {

    @Id
    @Column(name = "maHoaDon")
    private String maHoaDon;

    @Column(name = "maPhieuDat")
    private String maPhieuDat;

    @Column(name = "maKhuyenMai")
    private String maKhuyenMai;

    @Column(name = "tongTien")
    private double tongTien;

    @Column(name = "ngayLap")
    private LocalDateTime ngayLap;

    @Column(name = "billThanhToan")
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
