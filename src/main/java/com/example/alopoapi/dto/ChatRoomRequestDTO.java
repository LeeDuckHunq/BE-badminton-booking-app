package com.example.alopoapi.dto;

public class ChatRoomRequestDTO {

    private String username1;
    private String username2;

    //region constructor

    public ChatRoomRequestDTO() {
    }

    public ChatRoomRequestDTO(String username1, String username2) {
        this.username1 = username1;
        this.username2 = username2;
    }
    //endregion

    //region getter-setter

    public String getUsername1() {
        return username1;
    }

    public void setUsername1(String username1) {
        this.username1 = username1;
    }

    public String getUsername2() {
        return username2;
    }

    public void setUsername2(String username2) {
        this.username2 = username2;
    }

    //endregion
}
