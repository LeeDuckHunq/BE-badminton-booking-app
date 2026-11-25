package com.example.alopoapi.entity.nhom;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/** Hội nhóm cầu lông. Chat nhóm dùng phòng "NHOM_" + maNhom trong collection TinNhan. */
@Document(collection = "Nhom")
public class NhomEntity {

    @Id
    private String maNhom;

    @Field("tenNhom")
    private String tenNhom;

    @Field("moTa")
    private String moTa;

    /** Khu vực hay chơi, ví dụ "Quận 7, TP.HCM". */
    @Field("khuVuc")
    private String khuVuc;

    /** Trình độ chính (YEU, TB_YEU, TB, TB_KHA, KHA, GIOI); null = mọi trình độ. */
    @Field("trinhDo")
    private String trinhDo;

    /** Công khai: hiện ở "Khám phá nhóm", người ngoài gửi yêu cầu tham gia. Riêng tư: chỉ vào bằng mã mời. */
    @Field("congKhai")
    private boolean congKhai;

    @Field("maMoi")
    private String maMoi;

    /** Màu ảnh đại diện (0..5). */
    @Field("mau")
    private int mau;

    @Field("nguoiTao")
    private String nguoiTao;

    @Field("ngayTao")
    private LocalDateTime ngayTao;

    /** Admin khoá nhóm vi phạm. */
    @Field("khoa")
    private boolean khoa;

    public String getMaNhom() { return maNhom; }
    public void setMaNhom(String maNhom) { this.maNhom = maNhom; }
    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String khuVuc) { this.khuVuc = khuVuc; }
    public String getTrinhDo() { return trinhDo; }
    public void setTrinhDo(String trinhDo) { this.trinhDo = trinhDo; }
    public boolean isCongKhai() { return congKhai; }
    public void setCongKhai(boolean congKhai) { this.congKhai = congKhai; }
    public String getMaMoi() { return maMoi; }
    public void setMaMoi(String maMoi) { this.maMoi = maMoi; }
    public int getMau() { return mau; }
    public void setMau(int mau) { this.mau = mau; }
    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public boolean isKhoa() { return khoa; }
    public void setKhoa(boolean khoa) { this.khoa = khoa; }

    public String chatRoomId() { return "NHOM_" + maNhom; }
}
