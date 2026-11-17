package com.example.alopoapi.service;

import com.example.alopoapi.dto.SendMessageDTO;
import com.example.alopoapi.entity.chat.PhongChatEntity;
import com.example.alopoapi.entity.chat.ThanhVienPhongChatEntity;
import com.example.alopoapi.entity.chat.TinNhanEntity;
import com.example.alopoapi.repository.PhongChatRepo;
import com.example.alopoapi.repository.ThanhVienPhongChatRepo;
import com.example.alopoapi.repository.TinNhanRepo;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final PhongChatRepo phongChatRepo;
    private final ThanhVienPhongChatRepo tvRepo;
    private final TinNhanRepo tinNhanRepo;
    private final UserRepo userRepo;

    public ChatService(
            PhongChatRepo phongChatRepo,
            ThanhVienPhongChatRepo tvRepo,
            TinNhanRepo tinNhanRepo,
            UserRepo userRepo
    ) {
        this.phongChatRepo = phongChatRepo;
        this.tvRepo = tvRepo;
        this.tinNhanRepo = tinNhanRepo;
        this.userRepo = userRepo;
    }

    /** Lấy hoặc tạo phòng chat riêng giữa người đang đăng nhập và {@code otherUsername}. */
    public String getOrCreateRoom(String currentUsername, String otherUsername) {
        String other = userRepo.findByUsernameIgnoreCase(otherUsername == null ? "" : otherUsername.trim())
                .map(user -> user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người nhận"));
        if (other.equalsIgnoreCase(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tự nhắn tin cho chính mình");
        }

        Set<String> roomsOfFirstUser = tvRepo
                .findByUsername(currentUsername)
                .stream()
                .map(ThanhVienPhongChatEntity::getMaPhongChat)
                .collect(Collectors.toSet());

        String room = tvRepo
                .findByUsername(other)
                .stream()
                .map(ThanhVienPhongChatEntity::getMaPhongChat)
                .filter(roomsOfFirstUser::contains)
                .findFirst()
                .orElse(null);

        if (room != null) {
            return room;
        }

        String maPhongChat = "PC_" + UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PhongChatEntity phong = new PhongChatEntity();
        phong.setMaPhongChat(maPhongChat);
        phong.setLoaiPhong("PRIVATE");
        phong.setNgayTao(now);
        phongChatRepo.save(phong);

        tvRepo.save(new ThanhVienPhongChatEntity(null, maPhongChat, currentUsername, now));
        tvRepo.save(new ThanhVienPhongChatEntity(null, maPhongChat, other, now));

        return maPhongChat;
    }

    public List<TinNhanEntity> getMessages(String maPhongChat, String username) {
        requireMember(maPhongChat, username);
        return tinNhanRepo.findByMaPhongChatOrderByThoiGianGuiAsc(maPhongChat);
    }

    public void sendMessage(SendMessageDTO dto, String username) {
        if (dto == null || dto.getMaPhongChat() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu phòng chat");
        }
        String noiDung = dto.getNoiDung() == null ? "" : dto.getNoiDung().trim();
        if (noiDung.isEmpty() || noiDung.length() > MAX_MESSAGE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung tin nhắn không hợp lệ");
        }
        requireMember(dto.getMaPhongChat(), username);

        PhongChatEntity phong = phongChatRepo.findById(dto.getMaPhongChat())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng chat"));

        LocalDateTime now = LocalDateTime.now();
        TinNhanEntity tinNhan = new TinNhanEntity(
                "TN_" + UUID.randomUUID(),
                dto.getMaPhongChat(),
                username,
                noiDung,
                now
        );
        tinNhanRepo.save(tinNhan);

        phong.setTinNhanCuoi(noiDung);
        phong.setThoiGianTinNhanCuoi(now);
        phongChatRepo.save(phong);
    }

    private void requireMember(String maPhongChat, String username) {
        if (maPhongChat == null || !tvRepo.existsByMaPhongChatAndUsername(maPhongChat, username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không thuộc phòng chat này");
        }
    }
}
