package com.example.alopoapi.dto;

import java.time.LocalDateTime;

public class CreatePhieuDatRequest {

    private String maNguoiDung;
    private String maSan;
    private LocalDateTime batDau;
    private LocalDateTime ketThuc;
    private String maKhuyenMai;

    public CreatePhieuDatRequest() {
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(
            String maNguoiDung
    ) {
        this.maNguoiDung =
                maNguoiDung;
    }

    public String getMaSan() {
        return maSan;
    }

    public void setMaSan(
            String maSan
    ) {
        this.maSan = maSan;
    }

    public LocalDateTime getBatDau() {
        return batDau;
    }

    public void setBatDau(
            LocalDateTime batDau
    ) {
        this.batDau = batDau;
    }

    public LocalDateTime getKetThuc() {
        return ketThuc;
    }

    public void setKetThuc(
            LocalDateTime ketThuc
    ) {
        this.ketThuc = ketThuc;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(
            String maKhuyenMai
    ) {
        this.maKhuyenMai =
                maKhuyenMai;
    }
}