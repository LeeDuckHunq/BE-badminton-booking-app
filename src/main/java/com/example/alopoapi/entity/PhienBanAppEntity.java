package com.example.alopoapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Một bản phát hành app (_id = nenTang + "-" + soBuild). Bản có soBuild lớn nhất là bản mới nhất. */
@Document(collection = "PhienBanApp")
public class PhienBanAppEntity {

    @Id
    private String id;

    /** "android" */
    @Field("nenTang")
    private String nenTang;

    /** Tên phiên bản hiển thị, ví dụ "1.1.0". */
    @Field("phienBan")
    private String phienBan;

    /** Số build tăng dần (versionCode % 1000 trên Android). */
    @Field("soBuild")
    private int soBuild;

    /** App có build nhỏ hơn số này bắt buộc phải cập nhật mới dùng tiếp được. */
    @Field("buildToiThieu")
    private int buildToiThieu;

    @Field("thayDoi")
    private List<String> thayDoi = new ArrayList<>();

    /** Link tải theo kiến trúc CPU: "arm64-v8a", "armeabi-v7a", "x86_64", "universal". */
    @Field("taiVe")
    private Map<String, String> taiVe = new LinkedHashMap<>();

    /** Dung lượng (byte) theo kiến trúc. */
    @Field("kichThuoc")
    private Map<String, Long> kichThuoc = new LinkedHashMap<>();

    @Field("ngayPhatHanh")
    private LocalDateTime ngayPhatHanh;

    @Field("nguoiDang")
    private String nguoiDang;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNenTang() { return nenTang; }
    public void setNenTang(String nenTang) { this.nenTang = nenTang; }
    public String getPhienBan() { return phienBan; }
    public void setPhienBan(String phienBan) { this.phienBan = phienBan; }
    public int getSoBuild() { return soBuild; }
    public void setSoBuild(int soBuild) { this.soBuild = soBuild; }
    public int getBuildToiThieu() { return buildToiThieu; }
    public void setBuildToiThieu(int buildToiThieu) { this.buildToiThieu = buildToiThieu; }
    public List<String> getThayDoi() { return thayDoi == null ? new ArrayList<>() : thayDoi; }
    public void setThayDoi(List<String> thayDoi) { this.thayDoi = thayDoi; }
    public Map<String, String> getTaiVe() { return taiVe == null ? new LinkedHashMap<>() : taiVe; }
    public void setTaiVe(Map<String, String> taiVe) { this.taiVe = taiVe; }
    public Map<String, Long> getKichThuoc() { return kichThuoc == null ? new LinkedHashMap<>() : kichThuoc; }
    public void setKichThuoc(Map<String, Long> kichThuoc) { this.kichThuoc = kichThuoc; }
    public LocalDateTime getNgayPhatHanh() { return ngayPhatHanh; }
    public void setNgayPhatHanh(LocalDateTime ngayPhatHanh) { this.ngayPhatHanh = ngayPhatHanh; }
    public String getNguoiDang() { return nguoiDang; }
    public void setNguoiDang(String nguoiDang) { this.nguoiDang = nguoiDang; }
}
