package com.example.alopoapi.entity.voucher;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "ChiTietKhuyenMai")
public class ChiTietKhuyenMaiEntity {

    @Id
    private String id;

    @Field("maKM")
    private String maKM;

    @Field("maNguoiDung")
    private String maNguoiDung;

    //region constructor

    public ChiTietKhuyenMaiEntity() {
    }

    public ChiTietKhuyenMaiEntity(String id, String maKM, String maNguoiDung) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //endregion
}
