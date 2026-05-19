package com.example.alopoapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "HoaDon")
public class HoaDonEntity {

    @Id
    @Column(name = "maHoaDon")
    private String maHoaDon;

    @Column(name = "maPhieuDat")
    private String maPhieuDat;

    @Column(name = "maKhuyenMai")
    private String maKhuyenMai;

    @Column(name = "tongTien")
    private String tongTien;

    @Column(name = "ngayLap")
    private LocalDateTime ngayLap;

    @Column(name = "billThanhToan")
    private String billThanhToan;


}
