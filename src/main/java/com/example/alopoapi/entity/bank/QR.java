package com.example.alopoapi.entity.bank;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "QR")
public class QR {

    @Id
    private String Receiver;

    @Field("QRcode")
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
