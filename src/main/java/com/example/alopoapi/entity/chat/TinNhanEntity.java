package com.example.alopoapi.entity.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "TinNhan")
public class TinNhanEntity {

    @Id
    private String maTinNhan;

    @Field("maPhongChat")
    private String maPhongChat;

    @Field("usernameNguoiGui")
    private String usernameNguoiGui;

    @Field("noiDung")
    private String noiDung;

    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Ho_Chi_Minh"
    )
    @Field("thoiGianGui")
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
