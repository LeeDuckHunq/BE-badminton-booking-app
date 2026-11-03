package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalTime;

@Document(collection = "CumSan")
public class CumSanEntity {

    @Id
    private String maCumSan;

    @Field("tenCumSan")
    private String tenCumSan;

    @Field("diaChi")
    private String diaChi;

    @Field("moTa")
    private String moTa;

    @Field("sDT")
    private String sDT;

    @Field("gioMoCua")
    private LocalTime gioMoCua;

    @Field("gioDongCua")
    private LocalTime gioDongCua;

    @Field("trangThai")
    private String trangThai;
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
        this.trangThai = "ACTIVE";
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    //endregion
}
