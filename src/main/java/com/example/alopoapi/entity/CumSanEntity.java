package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "CumSan")
public class CumSanEntity {

    @Id
    @Column(name = "maCumSan")
    private String maCumSan;

    @Column(name = "tenCumSan")
    private String tenCumSan;

    @Column(name = "diaChi")
    private String diaChi;

    @Column(name = "moTa")
    private String moTa;

    @Column(name = "sDT")
    private String sDT;

    @Column(name = "gioMoCua")
    private LocalDateTime gioMoCua;

    @Column(name = "gioDongCua")
    private LocalDateTime gioDongCua;

}
