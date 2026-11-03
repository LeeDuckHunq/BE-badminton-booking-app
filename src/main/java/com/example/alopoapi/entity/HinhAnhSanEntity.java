package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "HinhAnhSan")
public class HinhAnhSanEntity {

    @Id
    private String maHinhAnh;

    @Field("duongDanHinhAnh")
    private String duongDanHinhAnh;

    @Field("maCumSan")
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
