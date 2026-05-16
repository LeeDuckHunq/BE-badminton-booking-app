package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "QR")
public class QR {

    @Id
    @Column(name = "Receiver")
    private String Receiver;

    @Column(name = "QRcode")
    private String QRcode;

    //region constructor
    public QR() {
    }

    public QR(String receiver, String QRcode) {
        Receiver = receiver;
        this.QRcode = QRcode;
    }

    //endregion

    //region getter - setter

    public String getQRcode() {
        return QRcode;
    }

    public void setQRcode(String QRcode) {
        this.QRcode = QRcode;
    }

    public String getReceiver() {
        return Receiver;
    }

    public void setReceiver(String receiver) {
        Receiver = receiver;
    }

    //endregion
}
