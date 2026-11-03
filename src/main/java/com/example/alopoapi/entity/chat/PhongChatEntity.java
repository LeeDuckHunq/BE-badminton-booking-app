package com.example.alopoapi.entity.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "PhongChat")
public class PhongChatEntity {

    @Id
    private String maPhongChat;

    @Field("loaiPhong")
    private String loaiPhong;

    @Field("ngayTao")
    private LocalDateTime ngayTao;

    @Field("tinNhanCuoi")
    private String tinNhanCuoi;

    @Field("thoiGianTinNhanCuoi")
    private LocalDateTime thoiGianTinNhanCuoi;

    //region constructor

    public PhongChatEntity() {
    }

    public PhongChatEntity(String maPhongChat, String loaiPhong,
                           LocalDateTime ngayTao, String tinNhanCuoi,
                           LocalDateTime thoiGianTinNhanCuoi) {
        this.maPhongChat = maPhongChat;
        this.loaiPhong = loaiPhong;
        this.ngayTao = ngayTao;
        this.tinNhanCuoi = tinNhanCuoi;
        this.thoiGianTinNhanCuoi = thoiGianTinNhanCuoi;
    }
    //endregion

    //region getter-setter

    public String getMaPhongChat() {
        return maPhongChat;
    }

    public void setMaPhongChat(String maPhongChat) {
        this.maPhongChat = maPhongChat;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTinNhanCuoi() {
        return tinNhanCuoi;
    }

    public void setTinNhanCuoi(String tinNhanCuoi) {
        this.tinNhanCuoi = tinNhanCuoi;
    }

    public LocalDateTime getThoiGianTinNhanCuoi() {
        return thoiGianTinNhanCuoi;
    }

    public void setThoiGianTinNhanCuoi(LocalDateTime thoiGianTinNhanCuoi) {
        this.thoiGianTinNhanCuoi = thoiGianTinNhanCuoi;
    }

    //endregion
}
