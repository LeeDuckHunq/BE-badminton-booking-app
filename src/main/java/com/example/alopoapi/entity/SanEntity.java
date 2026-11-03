package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "San")
public class SanEntity {

    @Id
    private String maSan;

    @Field("tenSan")
    private String tenSan;

    @Field("gia")
    private double gia;

    @Field("trangThai")
    private boolean trangThai;

    @Field("maCumSan")
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
