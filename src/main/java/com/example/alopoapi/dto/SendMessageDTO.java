package com.example.alopoapi.dto;

public class SendMessageDTO {
    private String maPhongChat;
    private String usernameNguoiGui;
    private String noiDung;

    //region constructor

    public SendMessageDTO() {
    }

    public SendMessageDTO(String maPhongChat, String usernameNguoiGui, String noiDung) {
        this.maPhongChat = maPhongChat;
        this.usernameNguoiGui = usernameNguoiGui;
        this.noiDung = noiDung;
    }
    //endregion

    //region getter-setter

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

    //endregion
}
