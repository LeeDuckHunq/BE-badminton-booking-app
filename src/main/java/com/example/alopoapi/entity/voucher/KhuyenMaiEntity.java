package com.example.alopoapi.entity.voucher;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "KhuyenMai")
public class KhuyenMaiEntity {

    @Id
    private String maKhuyenMai;

    @Field("tenKhuyenMai")
    private String tenKhuyenMai;

    @Field("duongDanAnh")
    private String duongDanAnh;

    @Field("phanTramGiam")
    private double phanTramGiam;

    @Field("ngayBatDau")
    private LocalDateTime ngayBatDau;

    @Field("ngayKetThuc")
    private LocalDateTime ngayKetThuc;

    //region constructor

    public KhuyenMaiEntity() {
    }

    public KhuyenMaiEntity(String maKhuyenMai, String tenKhuyenMai, String duongDanAnh,
                           double phanTramGiam, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.duongDanAnh = duongDanAnh;
        this.phanTramGiam = phanTramGiam;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    //endregion

    //region getter - setter

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getDuongDanAnh() {
        return duongDanAnh;
    }

    public void setDuongDanAnh(String duongDanAnh) {
        this.duongDanAnh = duongDanAnh;
    }

    public double getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(double phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }
    //endregion
}
