package com.example.alopoapi.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "PhongChat")
public class PhongChatEntity {

    @Id
    @Column(name = "maPhongChat", length = 20)
    private String maPhongChat;

    @Column(name = "loaiPhong", length = 20)
    private String loaiPhong;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "tinNhanCuoi", columnDefinition = "TEXT")
    private String tinNhanCuoi;

    @Column(name = "thoiGianTinNhanCuoi")
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
