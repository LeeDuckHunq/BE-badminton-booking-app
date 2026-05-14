package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "CumSan")
public class CumSanEntity {

    @Id
    @Column(name = "maCumSan")
    private String maCumSan;

    @Column(name = "tenCumSan")
    private String tenCumSan;

    @Column(name = "diaChi")
    private String diaChi;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "sDT")
    private String sDT;

    @Column(name = "gioMoCua")
    private LocalTime gioMoCua;

    @Column(name = "gioDongCua")
    private LocalTime gioDongCua;

    //region constructor

    public CumSanEntity() {
    }

    public CumSanEntity(String maCumSan, String tenCumSan, String diaChi,
                        String moTa, String sDT, LocalTime gioMoCua, LocalTime gioDongCua) {
        this.maCumSan = maCumSan;
        this.tenCumSan = tenCumSan;
        this.diaChi = diaChi;
        this.moTa = moTa;
        this.sDT = sDT;
        this.gioMoCua = gioMoCua;
        this.gioDongCua = gioDongCua;
    }
    //endregion

    //region getter - setter

    public String getMaCumSan() {
        return maCumSan;
    }

    public void setMaCumSan(String maCumSan) {
        this.maCumSan = maCumSan;
    }

    public String getTenCumSan() {
        return tenCumSan;
    }

    public void setTenCumSan(String tenCumSan) {
        this.tenCumSan = tenCumSan;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getsDT() {
        return sDT;
    }

    public void setsDT(String sDT) {
        this.sDT = sDT;
    }

    public LocalTime getGioMoCua() {
        return gioMoCua;
    }

    public void setGioMoCua(LocalTime gioMoCua) {
        this.gioMoCua = gioMoCua;
    }

    public LocalTime getGioDongCua() {
        return gioDongCua;
    }

    public void setGioDongCua(LocalTime gioDongCua) {
        this.gioDongCua = gioDongCua;
    }

    //endregion
}
