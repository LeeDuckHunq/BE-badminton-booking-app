package com.example.alopoapi.controller;

import com.example.alopoapi.dto.SendMessageDTO;
import com.example.alopoapi.dto.request.ChatRoomRequestDTO;
import com.example.alopoapi.entity.chat.TinNhanEntity;
import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** Một trong hai username phải là người đang đăng nhập; phòng được tạo giữa người đó và người còn lại. */
    @PostMapping("/get-or-create-room")
    public ResponseEntity<String> getOrCreateRoom(@RequestBody ChatRoomRequestDTO request, Authentication authentication) {
        String me = AccessGuard.username(authentication);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu thông tin phòng chat");
        }
        String other;
        if (me.equalsIgnoreCase(request.getUsername1())) {
            other = request.getUsername2();
        } else if (me.equalsIgnoreCase(request.getUsername2())) {
            other = request.getUsername1();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ có thể tạo phòng chat cho chính mình");
        }
        return ResponseEntity.ok(chatService.getOrCreateRoom(me, other));
    }

    @GetMapping("/messages/{maPhongChat}")
    public ResponseEntity<List<TinNhanEntity>> getMessages(@PathVariable String maPhongChat, Authentication authentication) {
        return ResponseEntity.ok(chatService.getMessages(maPhongChat, AccessGuard.username(authentication)));
    }

    /** Người gửi luôn là tài khoản đang đăng nhập; trường usernameNguoiGui trong body bị bỏ qua. */
    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody SendMessageDTO dto, Authentication authentication) {
        chatService.sendMessage(dto, AccessGuard.username(authentication));
        return ResponseEntity.ok("Gửi thành công");
    }
}
