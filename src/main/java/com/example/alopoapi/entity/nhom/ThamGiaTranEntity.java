package com.example.alopoapi.entity.nhom;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/** Một người trong trận (_id = maTran + "_" + username). */
@Document(collection = "ThamGiaTran")
public class ThamGiaTranEntity {

    /** trangThai */
    public static final String THAM_GIA = "THAM_GIA";
    /** Danh sách chờ (thành viên nhóm khi trận đã đủ). */
    public static final String CHO = "CHO";
    /** Người ngoài xin vào, chờ chủ trận duyệt. */
    public static final String CHO_DUYET = "CHO_DUYET";
    /** Báo vắng / đã rời trận. */
    public static final String VANG = "VANG";
    /** Bị từ chối hoặc bị mời ra. */
    public static final String TU_CHOI = "TU_CHOI";

    /** thanhToan */
    public static final String CHUA = "CHUA";
    public static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    public static final String DA_TRA = "DA_TRA";
    /** Chủ trận không phải góp. */
    public static final String MIEN = "MIEN";

    @Id
    private String id;

    @Field("maTran")
    private String maTran;

    @Field("username")
    private String username;

    @Field("trangThai")
    private String trangThai;

    @Field("loiNhan")
    private String loiNhan;

    /** Trình độ tự khai khi xin vào trận. */
    @Field("trinhDo")
    private String trinhDo;

    @Field("soTien")
    private long soTien;

    @Field("thanhToan")
    private String thanhToan;

    @Field("billUrl")
    private String billUrl;

    @Field("ngayThamGia")
    private LocalDateTime ngayThamGia;

    @Field("ngayCapNhat")
    private LocalDateTime ngayCapNhat;

    public static String idOf(String maTran, String username) { return maTran + "_" + username; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaTran() { return maTran; }
    public void setMaTran(String maTran) { this.maTran = maTran; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getLoiNhan() { return loiNhan; }
    public void setLoiNhan(String loiNhan) { this.loiNhan = loiNhan; }
    public String getTrinhDo() { return trinhDo; }
    public void setTrinhDo(String trinhDo) { this.trinhDo = trinhDo; }
    public long getSoTien() { return soTien; }
    public void setSoTien(long soTien) { this.soTien = soTien; }
    public String getThanhToan() { return thanhToan; }
    public void setThanhToan(String thanhToan) { this.thanhToan = thanhToan; }
    public String getBillUrl() { return billUrl; }
    public void setBillUrl(String billUrl) { this.billUrl = billUrl; }
    public LocalDateTime getNgayThamGia() { return ngayThamGia; }
    public void setNgayThamGia(LocalDateTime ngayThamGia) { this.ngayThamGia = ngayThamGia; }
    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    public boolean isGoing() { return THAM_GIA.equals(trangThai); }
}
