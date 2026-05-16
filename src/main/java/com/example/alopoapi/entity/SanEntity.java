package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "San")
public class SanEntity {

    @Id
    @Column(name = "maSan")
    private String maSan;

    @Column(name = "tenSan")
    private String tenSan;

    @Column(name = "gia")
    private double gia;

    @Column(name = "trangThai")
    private boolean trangThai;

    @Column(name = "maCumSan")
    private String maCumSan;

    //region constructor

    public SanEntity() {
    }

    public SanEntity(String maSan, String tenSan, double gia, boolean trangThai, String maCumSan) {
        this.maSan = maSan;
        this.tenSan = tenSan;
        this.gia = gia;
        this.trangThai = trangThai;
        this.maCumSan = maCumSan;
    }
    //endregion

    //region getter - setter

    public String getMaSan() {
        return maSan;
    }

    public void setMaSan(String maSan) {
        this.maSan = maSan;
    }

    public String getTenSan() {
        return tenSan;
    }

    public void setTenSan(String tenSan) {
        this.tenSan = tenSan;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaCumSan() {
        return maCumSan;
    }

    public void setMaCumSan(String maCumSan) {
        this.maCumSan = maCumSan;
    }

    //endregion
}
