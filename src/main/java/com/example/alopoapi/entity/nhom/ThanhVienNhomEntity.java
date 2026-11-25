package com.example.alopoapi.entity.nhom;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/** Thành viên nhóm (_id = maNhom + "_" + username). */
@Document(collection = "ThanhVienNhom")
public class ThanhVienNhomEntity {

    /** vaiTro */
    public static final String TRUONG_NHOM = "TRUONG_NHOM";
    public static final String PHO_NHOM = "PHO_NHOM";
    public static final String THANH_VIEN = "THANH_VIEN";

    /** trangThai */
    public static final String CHO_DUYET = "CHO_DUYET";
    public static final String DA_VAO = "THANH_VIEN";

    @Id
    private String id;

    @Field("maNhom")
    private String maNhom;

    @Field("username")
    private String username;

    @Field("vaiTro")
    private String vaiTro;

    /** CHO_DUYET hoặc THANH_VIEN. */
    @Field("trangThai")
    private String trangThai;

    /** Lời nhắn khi xin vào nhóm. */
    @Field("loiNhan")
    private String loiNhan;

    @Field("ngayThamGia")
    private LocalDateTime ngayThamGia;

    public static String idOf(String maNhom, String username) { return maNhom + "_" + username; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaNhom() { return maNhom; }
    public void setMaNhom(String maNhom) { this.maNhom = maNhom; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getLoiNhan() { return loiNhan; }
    public void setLoiNhan(String loiNhan) { this.loiNhan = loiNhan; }
    public LocalDateTime getNgayThamGia() { return ngayThamGia; }
    public void setNgayThamGia(LocalDateTime ngayThamGia) { this.ngayThamGia = ngayThamGia; }

    public boolean isActive() { return DA_VAO.equals(trangThai); }
    public boolean isLeader() { return isActive() && (TRUONG_NHOM.equals(vaiTro) || PHO_NHOM.equals(vaiTro)); }
    public boolean isOwner() { return isActive() && TRUONG_NHOM.equals(vaiTro); }
}
