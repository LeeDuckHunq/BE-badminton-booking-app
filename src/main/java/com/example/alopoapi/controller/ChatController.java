package com.example.alopoapi.controller;

import com.example.alopoapi.dto.ChatRoomRequestDTO;
import com.example.alopoapi.dto.SendMessageDTO;
import com.example.alopoapi.entity.chat.TinNhanEntity;
import com.example.alopoapi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/get-or-create-room")
    public ResponseEntity<String>
    getOrCreateRoom(
            @RequestBody
            ChatRoomRequestDTO request
    ) {

        return ResponseEntity.ok(
                chatService
                        .getOrCreateRoom(request)
        );
    }

    @GetMapping("/messages/{maPhongChat}")
    public ResponseEntity<List<TinNhanEntity>>
    getMessages(
            @PathVariable String maPhongChat
    ) {

        return ResponseEntity.ok(
                chatService
                        .getMessages(maPhongChat)
        );
    }

    @PostMapping("/send-message")
    public ResponseEntity<String>
    sendMessage(
            @RequestBody
            SendMessageDTO dto
    ) {

        chatService.sendMessage(dto);

        return ResponseEntity.ok(
                "Gửi thành công"
        );
    }
}
