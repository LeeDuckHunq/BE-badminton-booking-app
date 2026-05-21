package com.example.alopoapi.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "TinNhan")
public class TinNhanEntity {

    @Id
    @Column(name = "maTinNhan", length = 20)
    private String maTinNhan;

    @Column(name = "maPhongChat", length = 20)
    private String maPhongChat;

    @Column(name = "usernameNguoiGui", length = 50)
    private String usernameNguoiGui;

    @Column(name = "noiDung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "thoiGianGui")
    private LocalDateTime thoiGianGui;

    //region constructor

    public TinNhanEntity() {
    }

    public TinNhanEntity(String maTinNhan, String maPhongChat,
                         String usernameNguoiGui, String noiDung, LocalDateTime thoiGianGui) {
        this.maTinNhan = maTinNhan;
        this.maPhongChat = maPhongChat;
        this.usernameNguoiGui = usernameNguoiGui;
        this.noiDung = noiDung;
        this.thoiGianGui = thoiGianGui;
    }
    //endregion

    //region getter-setter

    public String getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(String maTinNhan) {
        this.maTinNhan = maTinNhan;
    }

    public String getMaPhongChat() {
        return maPhongChat;
    }

    public void setMaPhongChat(String maPhongChat) {
        this.maPhongChat = maPhongChat;
    }

    public String getUsernameNguoiGui() {
        return usernameNguoiGui;
    }

    public void setUsernameNguoiGui(String usernameNguoiGui) {
        this.usernameNguoiGui = usernameNguoiGui;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(LocalDateTime thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }

    //endregion
}
