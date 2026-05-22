package com.example.alopoapi.entity.voucher;

import jakarta.persistence.*;

@Entity
@Table(name = "ChiTietKhuyenMai")
public class ChiTietKhuyenMaiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "maKM")
    private String maKM;

    @Column(name = "maNguoiDung")
    private String maNguoiDung;

    //region constructor

    public ChiTietKhuyenMaiEntity() {
    }

    public ChiTietKhuyenMaiEntity(int id, String maKM, String maNguoiDung) {
        this.id = id;
        this.maKM = maKM;
        this.maNguoiDung = maNguoiDung;
    }

    //endregion

    //region getter - setter

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //endregion
}
