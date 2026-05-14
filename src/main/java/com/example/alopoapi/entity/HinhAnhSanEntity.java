package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "HinhAnhSan")
public class HinhAnhSanEntity {

    @Id
    @Column(name = "maHinhAnh")
    private String maHinhAnh;

    @Column(name = "duongDanHinhAnh")
    private String duongDanHinhAnh;

    @Column(name = "maCumSan")
    private String maCumSan;

    //region constructor

    public HinhAnhSanEntity() {
    }

    public HinhAnhSanEntity(String maHinhAnh, String duongDanHinhAnh, String maCumSan) {
        this.maHinhAnh = maHinhAnh;
        this.duongDanHinhAnh = duongDanHinhAnh;
        this.maCumSan = maCumSan;
    }
    //endregion

    //region getter - setter

    public String getMaHinhAnh() {
        return maHinhAnh;
    }

    public void setMaHinhAnh(String maHinhAnh) {
        this.maHinhAnh = maHinhAnh;
    }

    public String getDuongDanHinhAnh() {
        return duongDanHinhAnh;
    }

    public void setDuongDanHinhAnh(String duongDanHinhAnh) {
        this.duongDanHinhAnh = duongDanHinhAnh;
    }

    public String getMaCumSan() {
        return maCumSan;
    }

    public void setMaCumSan(String maCumSan) {
        this.maCumSan = maCumSan;
    }

    //endregion
}
