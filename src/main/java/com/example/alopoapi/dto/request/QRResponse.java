package com.example.alopoapi.dto.request;

public class QRResponse {

    private String receiver;
    private String qrCode;

    public QRResponse() {
    }

    public QRResponse(String receiver, String qrCode) {
        this.receiver = receiver;
        this.qrCode = qrCode;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}