package com.example.alopoapi.entity.chat;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ThanhVienPhongChat")
public class ThanhVienPhongChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maPhongChat", length = 20)
    private String maPhongChat;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "ngayThamGia")
    private LocalDateTime ngayThamGia;

    //region constructor

    public ThanhVienPhongChatEntity() {
    }

    public ThanhVienPhongChatEntity(Integer id, String maPhongChat,
                                    String username, LocalDateTime ngayThamGia) {
        this.id = id;
        this.maPhongChat = maPhongChat;
        this.username = username;
        this.ngayThamGia = ngayThamGia;
    }
    //endregion

    //region getter-setter

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
