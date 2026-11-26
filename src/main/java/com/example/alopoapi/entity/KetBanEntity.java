package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Quan hệ bạn bè giữa hai người dùng. _id = hai username (chữ thường) xếp theo thứ tự, nối bằng "|",
 * nên mỗi cặp chỉ có một bản ghi dù ai gửi lời mời trước.
 */
@Document(collection = "KetBan")
public class KetBanEntity {

    public static final String CHO = "CHO";
    public static final String BAN_BE = "BAN_BE";

    @Id
    private String id;
    private String nguoiGui;
    private String nguoiNhan;
    private String trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayKetBan;

    public static String idOf(String a, String b) {
        String x = a.toLowerCase(Locale.ROOT), y = b.toLowerCase(Locale.ROOT);
        return x.compareTo(y) < 0 ? x + "|" + y : y + "|" + x;
    }

    /** Người còn lại trong quan hệ. */
    public String other(String me) {
        return nguoiGui.equalsIgnoreCase(me) ? nguoiNhan : nguoiGui;
    }

    public boolean isFriend() { return BAN_BE.equals(trangThai); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNguoiGui() { return nguoiGui; }
    public void setNguoiGui(String nguoiGui) { this.nguoiGui = nguoiGui; }
    public String getNguoiNhan() { return nguoiNhan; }
    public void setNguoiNhan(String nguoiNhan) { this.nguoiNhan = nguoiNhan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public LocalDateTime getNgayKetBan() { return ngayKetBan; }
    public void setNgayKetBan(LocalDateTime ngayKetBan) { this.ngayKetBan = ngayKetBan; }
}
