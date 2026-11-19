package com.example.alopoapi.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Transient;

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

    @Field("ownerUsername")
    private String ownerUsername;

    /** Giá thấp nhất / giờ trong các sân đang hoạt động (tính khi trả về, không lưu DB). */
    @Transient
    private Double giaTu;

    /** Số sân đang hoạt động (tính khi trả về, không lưu DB). */
    @Transient
    private Integer soSan;

    /** Điểm đánh giá trung bình 1..5 (null nếu chưa có), tính khi trả về. */
    @Transient
    private Double diemDanhGia;

    @Transient
    private Integer soDanhGia;

    public Double getDiemDanhGia() {
        return diemDanhGia;
    }

    public void setDiemDanhGia(Double diemDanhGia) {
        this.diemDanhGia = diemDanhGia;
    }

    public Integer getSoDanhGia() {
        return soDanhGia;
    }

    public void setSoDanhGia(Integer soDanhGia) {
        this.soDanhGia = soDanhGia;
    }
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

    @JsonProperty("sDT")
    public String getsDT() {
        return sDT;
    }

    @JsonProperty("sDT")
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

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
    public Double getGiaTu() {
        return giaTu;
    }

    public void setGiaTu(Double giaTu) {
        this.giaTu = giaTu;
    }

    public Integer getSoSan() {
        return soSan;
    }

    public void setSoSan(Integer soSan) {
        this.soSan = soSan;
    }
    //endregion
}
