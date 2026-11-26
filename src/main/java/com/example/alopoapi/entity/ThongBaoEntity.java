package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Thông báo gửi tới một người dùng (khách, chủ sân).
 * <ul>
 *   <li>{@code daDoc}: người dùng đã mở / đánh dấu đọc (web, app, dashboard dùng chung).</li>
 *   <li>{@code daGui}: đã đẩy lên điện thoại thành thông báo hệ thống (app Android lấy qua /thong-bao/chua-gui).</li>
 *   <li>Tin nhắn từ cùng một người gộp vào một thông báo chưa đọc ({@code soLuong} tăng dần).</li>
 * </ul>
 */
@Document(collection = "ThongBao")
@CompoundIndex(name = "nguoiNhan_thoiGian", def = "{'nguoiNhan': 1, 'thoiGian': -1}")
public class ThongBaoEntity {

    // Loại thông báo
    public static final String DAT_SAN_XAC_NHAN = "DAT_SAN_XAC_NHAN";
    public static final String DAT_SAN_HUY = "DAT_SAN_HUY";
    public static final String DAT_SAN_HOAN_THANH = "DAT_SAN_HOAN_THANH";
    public static final String DAT_SAN_DOI_SAN = "DAT_SAN_DOI_SAN";
    public static final String DAT_HO = "DAT_HO";
    public static final String DON_MOI = "DON_MOI";
    public static final String YEU_CAU_HUY = "YEU_CAU_HUY";
    public static final String HUY_BI_TU_CHOI = "HUY_BI_TU_CHOI";
    public static final String TIN_NHAN = "TIN_NHAN";
    public static final String TRAN_XIN_VAO = "TRAN_XIN_VAO";
    public static final String TRAN_DUOC_DUYET = "TRAN_DUOC_DUYET";
    public static final String TRAN_TU_CHOI = "TRAN_TU_CHOI";
    public static final String TRAN_HUY = "TRAN_HUY";
    public static final String TRAN_DOI_SAN = "TRAN_DOI_SAN";
    public static final String TRAN_LEN_DANH_SACH = "TRAN_LEN_DANH_SACH";
    public static final String TRAN_DA_CHUYEN_TIEN = "TRAN_DA_CHUYEN_TIEN";
    public static final String DANH_GIA_PHAN_HOI = "DANH_GIA_PHAN_HOI";
    public static final String KET_BAN_LOI_MOI = "KET_BAN_LOI_MOI";
    public static final String KET_BAN_DONG_Y = "KET_BAN_DONG_Y";

    // Đối tượng để client mở đúng màn hình khi bấm vào thông báo
    public static final String BOOKING = "BOOKING";
    public static final String CHAT = "CHAT";
    public static final String MATCH = "MATCH";
    public static final String REVIEW = "REVIEW";
    public static final String FRIEND = "FRIEND";

    @Id
    private String maThongBao;
    private String nguoiNhan;
    private String loai;
    private String tieuDe;
    private String noiDung;
    private String doiTuong;
    private String maDoiTuong;
    private String nguoiGui;
    private int soLuong = 1;
    private boolean daDoc;
    private boolean daGui;
    private LocalDateTime thoiGian;

    public String getMaThongBao() { return maThongBao; }
    public void setMaThongBao(String maThongBao) { this.maThongBao = maThongBao; }
    public String getNguoiNhan() { return nguoiNhan; }
    public void setNguoiNhan(String nguoiNhan) { this.nguoiNhan = nguoiNhan; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }
    public String getMaDoiTuong() { return maDoiTuong; }
    public void setMaDoiTuong(String maDoiTuong) { this.maDoiTuong = maDoiTuong; }
    public String getNguoiGui() { return nguoiGui; }
    public void setNguoiGui(String nguoiGui) { this.nguoiGui = nguoiGui; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public boolean isDaDoc() { return daDoc; }
    public void setDaDoc(boolean daDoc) { this.daDoc = daDoc; }
    public boolean isDaGui() { return daGui; }
    public void setDaGui(boolean daGui) { this.daGui = daGui; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}
