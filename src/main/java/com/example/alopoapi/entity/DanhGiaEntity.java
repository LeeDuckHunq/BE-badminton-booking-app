package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/** Đánh giá của khách cho một lần chơi (mỗi phiếu đặt tối đa 1 đánh giá; _id = "DG_" + maPhieuDat). */
@Document(collection = "DanhGia")
public class DanhGiaEntity {

    @Id
    private String maDanhGia;

    @Field("maPhieuDat")
    private String maPhieuDat;

    @Field("maCumSan")
    private String maCumSan;

    @Field("maSan")
    private String maSan;

    @Field("maNguoiDung")
    private String maNguoiDung;

    /** 1..5 */
    @Field("soSao")
    private int soSao;

    @Field("noiDung")
    private String noiDung;

    @Field("ngayTao")
    private LocalDateTime ngayTao;

    @Field("ngayCapNhat")
    private LocalDateTime ngayCapNhat;

    /** Trả lời công khai của chủ sân. */
    @Field("phanHoi")
    private String phanHoi;

    @Field("ngayPhanHoi")
    private LocalDateTime ngayPhanHoi;

    @Field("nguoiPhanHoi")
    private String nguoiPhanHoi;

    /** Admin ẩn đánh giá vi phạm. */
    @Field("an")
    private boolean an;

    public String getMaDanhGia() { return maDanhGia; }
    public void setMaDanhGia(String maDanhGia) { this.maDanhGia = maDanhGia; }
    public String getMaPhieuDat() { return maPhieuDat; }
    public void setMaPhieuDat(String maPhieuDat) { this.maPhieuDat = maPhieuDat; }
    public String getMaCumSan() { return maCumSan; }
    public void setMaCumSan(String maCumSan) { this.maCumSan = maCumSan; }
    public String getMaSan() { return maSan; }
    public void setMaSan(String maSan) { this.maSan = maSan; }
    public String getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(String maNguoiDung) { this.maNguoiDung = maNguoiDung; }
    public int getSoSao() { return soSao; }
    public void setSoSao(int soSao) { this.soSao = soSao; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
    public String getPhanHoi() { return phanHoi; }
    public void setPhanHoi(String phanHoi) { this.phanHoi = phanHoi; }
    public LocalDateTime getNgayPhanHoi() { return ngayPhanHoi; }
    public void setNgayPhanHoi(LocalDateTime ngayPhanHoi) { this.ngayPhanHoi = ngayPhanHoi; }
    public String getNguoiPhanHoi() { return nguoiPhanHoi; }
    public void setNguoiPhanHoi(String nguoiPhanHoi) { this.nguoiPhanHoi = nguoiPhanHoi; }
    public boolean isAn() { return an; }
    public void setAn(boolean an) { this.an = an; }
}
