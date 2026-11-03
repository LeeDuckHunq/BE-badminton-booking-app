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
    //endregion

}
