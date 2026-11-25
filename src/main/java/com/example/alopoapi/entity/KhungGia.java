package com.example.alopoapi.entity;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Một khung giá của sân (lưu nhúng trong San.bangGia): áp dụng cho các thứ {@code thu}
 * (1 = Thứ Hai … 7 = Chủ Nhật), từ {@code tuGio} đến trước {@code denGio} ("HH:mm", denGio có thể là "24:00").
 */
public class KhungGia {

    @Field("thu")
    private List<Integer> thu = new ArrayList<>();

    @Field("tuGio")
    private String tuGio;

    @Field("denGio")
    private String denGio;

    /** Giá mỗi giờ (VNĐ). */
    @Field("gia")
    private double gia;

    /** Tên gợi nhớ, ví dụ "Giờ cao điểm". */
    @Field("ten")
    private String ten;

    public KhungGia() {
    }

    public KhungGia(List<Integer> thu, String tuGio, String denGio, double gia, String ten) {
        this.thu = thu;
        this.tuGio = tuGio;
        this.denGio = denGio;
        this.gia = gia;
        this.ten = ten;
    }

    public List<Integer> getThu() { return thu; }
    public void setThu(List<Integer> thu) { this.thu = thu; }
    public String getTuGio() { return tuGio; }
    public void setTuGio(String tuGio) { this.tuGio = tuGio; }
    public String getDenGio() { return denGio; }
    public void setDenGio(String denGio) { this.denGio = denGio; }
    public double getGia() { return gia; }
    public void setGia(double gia) { this.gia = gia; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
}
