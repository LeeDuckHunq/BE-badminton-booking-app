package com.example.alopoapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "PhieuDatSan")
public class PhieuDatSanEntity {

    @Id
    @Column(name = "maPhieuDat")
    private String maPhieuDat;

    @Column(name = "maNguoiDung")
    private String maNguoiDung;

    @Column(name = "maSan")
    private String maSan;

    @Column(name = "batDau")
    private LocalDateTime batDau;

    @Column(name = "ketThuc")
    private LocalDateTime ketThuc;

    @Column(name = "tongTien")
    private double tongTien;

    @Column(name = "maKhuyenMai")
    private String maKhuyenMai;

    @Column(name = "ngayLap")
    private LocalDateTime ngayLap;

    @Column(name = "trangThai")
    private String trangThai;

    //region constructor

    public PhieuDatSanEntity() {
    }

    public PhieuDatSanEntity(String maPhieuDat, String maNguoiDung, String maSan,
                             LocalDateTime batDau, LocalDateTime ketThuc, double tongTien,
                             String maKhuyenMai, LocalDateTime ngayLap, String trangThai) {
        this.maPhieuDat = maPhieuDat;
        this.maNguoiDung = maNguoiDung;
        this.maSan = maSan;
        this.batDau = batDau;
        this.ketThuc = ketThuc;
        this.tongTien = tongTien;
        this.maKhuyenMai = maKhuyenMai;
        this.ngayLap = ngayLap;
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

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
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
    //endregion
}
