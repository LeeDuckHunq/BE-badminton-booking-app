package com.example.alopoapi.entity.nhom;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Trận / kèo: tạo từ một phiếu đặt sân của chủ trận. Có thể thuộc một nhóm (thành viên nhóm vào thẳng)
 * và/hoặc công khai để người ngoài tìm và xin vào (vãng lai).
 */
@Document(collection = "TranDau")
public class TranDauEntity {

    public static final String MO = "MO";
    public static final String DA_HUY = "DA_HUY";

    @Id
    private String maTran;

    @Field("maNhom")
    private String maNhom;

    @Field("maPhieuDat")
    private String maPhieuDat;

    @Field("maSan")
    private String maSan;

    @Field("maCumSan")
    private String maCumSan;

    @Field("batDau")
    private LocalDateTime batDau;

    @Field("ketThuc")
    private LocalDateTime ketThuc;

    /** Username chủ trận (người đặt sân). */
    @Field("chuTran")
    private String chuTran;

    @Field("tieuDe")
    private String tieuDe;

    @Field("ghiChu")
    private String ghiChu;

    /** DOI (đánh đôi), DON (đánh đơn), GIAO_LUU (xoay vòng). */
    @Field("kieuChoi")
    private String kieuChoi;

    /** Tính cả chủ trận. */
    @Field("soNguoiToiDa")
    private int soNguoiToiDa;

    @Field("trinhDoTu")
    private String trinhDoTu;

    @Field("trinhDoDen")
    private String trinhDoDen;

    /** Tiền mỗi người góp (VNĐ). */
    @Field("chiPhiMoiNguoi")
    private long chiPhiMoiNguoi;

    @Field("congKhai")
    private boolean congKhai;

    /** Thông tin nhận tiền của chủ trận (mã ngân hàng VietQR, số tài khoản, tên chủ tài khoản). */
    @Field("nganHang")
    private String nganHang;

    @Field("soTaiKhoan")
    private String soTaiKhoan;

    @Field("chuTaiKhoan")
    private String chuTaiKhoan;

    /** MO hoặc DA_HUY. Đủ người / đang diễn ra / đã kết thúc được tính khi trả về. */
    @Field("trangThai")
    private String trangThai;

    @Field("ngayTao")
    private LocalDateTime ngayTao;

    public String getMaTran() { return maTran; }
    public void setMaTran(String maTran) { this.maTran = maTran; }
    public String getMaNhom() { return maNhom; }
    public void setMaNhom(String maNhom) { this.maNhom = maNhom; }
    public String getMaPhieuDat() { return maPhieuDat; }
    public void setMaPhieuDat(String maPhieuDat) { this.maPhieuDat = maPhieuDat; }
    public String getMaSan() { return maSan; }
    public void setMaSan(String maSan) { this.maSan = maSan; }
    public String getMaCumSan() { return maCumSan; }
    public void setMaCumSan(String maCumSan) { this.maCumSan = maCumSan; }
    public LocalDateTime getBatDau() { return batDau; }
    public void setBatDau(LocalDateTime batDau) { this.batDau = batDau; }
    public LocalDateTime getKetThuc() { return ketThuc; }
    public void setKetThuc(LocalDateTime ketThuc) { this.ketThuc = ketThuc; }
    public String getChuTran() { return chuTran; }
    public void setChuTran(String chuTran) { this.chuTran = chuTran; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getKieuChoi() { return kieuChoi; }
    public void setKieuChoi(String kieuChoi) { this.kieuChoi = kieuChoi; }
    public int getSoNguoiToiDa() { return soNguoiToiDa; }
    public void setSoNguoiToiDa(int soNguoiToiDa) { this.soNguoiToiDa = soNguoiToiDa; }
    public String getTrinhDoTu() { return trinhDoTu; }
    public void setTrinhDoTu(String trinhDoTu) { this.trinhDoTu = trinhDoTu; }
    public String getTrinhDoDen() { return trinhDoDen; }
    public void setTrinhDoDen(String trinhDoDen) { this.trinhDoDen = trinhDoDen; }
    public long getChiPhiMoiNguoi() { return chiPhiMoiNguoi; }
    public void setChiPhiMoiNguoi(long chiPhiMoiNguoi) { this.chiPhiMoiNguoi = chiPhiMoiNguoi; }
    public boolean isCongKhai() { return congKhai; }
    public void setCongKhai(boolean congKhai) { this.congKhai = congKhai; }
    public String getNganHang() { return nganHang; }
    public void setNganHang(String nganHang) { this.nganHang = nganHang; }
    public String getSoTaiKhoan() { return soTaiKhoan; }
    public void setSoTaiKhoan(String soTaiKhoan) { this.soTaiKhoan = soTaiKhoan; }
    public String getChuTaiKhoan() { return chuTaiKhoan; }
    public void setChuTaiKhoan(String chuTaiKhoan) { this.chuTaiKhoan = chuTaiKhoan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
