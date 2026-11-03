package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Favorite")
public class FavoriteEntity {

    @Id
    private String maFav;

    @Field("maNguoiDung")
    private String maNguoiDung;

    @Field("maCumSan")
    private String maCumSan;

    //region constructor

    public FavoriteEntity() {
    }

    public FavoriteEntity(String maFav, String maNguoiDung, String maCumSan) {
        this.maFav = maFav;
        this.maNguoiDung = maNguoiDung;
        this.maCumSan = maCumSan;
    }
    //endregion

    //region getter-setter

    public String getMaFav() {
        return maFav;
    }

    public void setMaFav(String maFav) {
        this.maFav = maFav;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getMaCumSan() {
        return maCumSan;
    }

    public void setMaCumSan(String maCumSan) {
        this.maCumSan = maCumSan;
    }

    //endregion
}
