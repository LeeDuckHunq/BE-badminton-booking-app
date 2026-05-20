package com.example.alopoapi.dto;

public class TopCumSanDTO {

    private String maCumSan;
    private Long soLuongDat;

    public TopCumSanDTO(String maCumSan, Long soLuongDat) {
        this.maCumSan = maCumSan;
        this.soLuongDat = soLuongDat;
    }

    public String getMaCumSan() {
        return maCumSan;
    }

    public void setMaCumSan(String maCumSan) {
        this.maCumSan = maCumSan;
    }

    public Long getSoLuongDat() {
        return soLuongDat;
    }

    public void setSoLuongDat(Long soLuongDat) {
        this.soLuongDat = soLuongDat;
    }
}