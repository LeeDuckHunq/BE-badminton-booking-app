package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Favorite")
public class FavoriteEntity {

    @Id
    @Column(name = "maFav")
    private String maFav;

    @Column(name = "maNguoiDung")
    private String maNguoiDung;

    @Column(name = "maCumSan")
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
