package com.learningpath.controller;

import com.learningpath.dto.ChatMessageDto;
import com.learningpath.dto.ChatRequest;
import com.learningpath.dto.ChatResponse;
import com.learningpath.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDto>> getHistory() {
        return ResponseEntity.ok(chatService.getChatHistory());
    }

    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }
}
