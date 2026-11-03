package com.example.alopoapi.entity.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "ThanhVienPhongChat")
public class ThanhVienPhongChatEntity {

    @Id
    private String id;

    @Field("maPhongChat")
    private String maPhongChat;

    @Field("username")
    private String username;

    @Field("ngayThamGia")
    private LocalDateTime ngayThamGia;

    //region constructor

    public ThanhVienPhongChatEntity() {
    }

    public ThanhVienPhongChatEntity(String id, String maPhongChat,
                                    String username, LocalDateTime ngayThamGia) {
        this.id = id;
        this.maPhongChat = maPhongChat;
        this.username = username;
        this.ngayThamGia = ngayThamGia;
    }
    //endregion

    //region getter-setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaPhongChat() {
        return maPhongChat;
    }

    public void setMaPhongChat(String maPhongChat) {
        this.maPhongChat = maPhongChat;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getNgayThamGia() {
        return ngayThamGia;
    }

    public void setNgayThamGia(LocalDateTime ngayThamGia) {
        this.ngayThamGia = ngayThamGia;
    }

    //endregion
}
