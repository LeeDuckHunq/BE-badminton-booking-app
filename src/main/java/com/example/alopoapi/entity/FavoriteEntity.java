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

    @Column(name = "maSan")
    private String maSan;

    //region constructor

    public FavoriteEntity() {
    }

    public FavoriteEntity(String maFav, String maNguoiDung, String maSan) {
        this.maFav = maFav;
        this.maNguoiDung = maNguoiDung;
        this.maSan = maSan;
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

    public String getMaSan() {
        return maSan;
    }

    public void setMaSan(String maSan) {
        this.maSan = maSan;
    }

    //endregion
}
