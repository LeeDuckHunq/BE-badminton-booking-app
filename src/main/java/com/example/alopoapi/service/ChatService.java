package com.example.alopoapi.service;

import com.example.alopoapi.dto.request.ChatRoomRequestDTO;
import com.example.alopoapi.dto.SendMessageDTO;
import com.example.alopoapi.entity.chat.PhongChatEntity;
import com.example.alopoapi.entity.chat.ThanhVienPhongChatEntity;
import com.example.alopoapi.entity.chat.TinNhanEntity;
import com.example.alopoapi.repository.PhongChatRepo;
import com.example.alopoapi.repository.ThanhVienPhongChatRepo;
import com.example.alopoapi.repository.TinNhanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private PhongChatRepo phongChatRepo;

    @Autowired
    private ThanhVienPhongChatRepo tvRepo;

    @Autowired
    private TinNhanRepo tinNhanRepo;

    public String getOrCreateRoom(
            ChatRoomRequestDTO request
    ) {

        String room =
                tvRepo.findPrivateRoom(
                        request.getUsername1(),
                        request.getUsername2()
                );

        if (room != null) {
            return room;
        }

        String maPhongChat =
                "PC" + System.currentTimeMillis();

        PhongChatEntity phong =
                new PhongChatEntity();

        phong.setMaPhongChat(maPhongChat);
        phong.setLoaiPhong("PRIVATE");
        phong.setNgayTao(LocalDateTime.now());

        phongChatRepo.save(phong);

        tvRepo.save(
                new ThanhVienPhongChatEntity(
                        null,
                        maPhongChat,
                        request.getUsername1(),
                        LocalDateTime.now()
                )
        );

        tvRepo.save(
                new ThanhVienPhongChatEntity(
                        null,
                        maPhongChat,
                        request.getUsername2(),
                        LocalDateTime.now()
                )
        );

        return maPhongChat;
    }

    public List<TinNhanEntity>
    getMessages(String maPhongChat) {

        return tinNhanRepo
                .findByMaPhongChatOrderByThoiGianGuiAsc(
                        maPhongChat
                );
    }

    public void sendMessage(
            SendMessageDTO dto
    ) {

        TinNhanEntity tinNhan =
                new TinNhanEntity();

        tinNhan.setMaTinNhan(
                "TN" + System.currentTimeMillis()
        );

        tinNhan.setMaPhongChat(
                dto.getMaPhongChat()
        );

        tinNhan.setUsernameNguoiGui(
                dto.getUsernameNguoiGui()
        );

        tinNhan.setNoiDung(
                dto.getNoiDung()
        );

        tinNhan.setThoiGianGui(
                LocalDateTime.now()
        );

        tinNhanRepo.save(tinNhan);

        PhongChatEntity phong =
                phongChatRepo
                        .findById(
                                dto.getMaPhongChat()
                        )
                        .orElseThrow();

        phong.setTinNhanCuoi(
                dto.getNoiDung()
        );

        phong.setThoiGianTinNhanCuoi(
                LocalDateTime.now()
        );

        phongChatRepo.save(phong);
    }
}
