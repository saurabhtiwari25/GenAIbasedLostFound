package com.my.lostfound.controller;

import com.my.lostfound.dto.MessageRequestDto;
import com.my.lostfound.dto.MessageResponseDto;
import com.my.lostfound.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.my.lostfound.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(@Valid @RequestBody MessageRequestDto request) {
        MessageResponseDto response = messageService.sendMessage(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<MessageResponseDto>> getMyMessages(@AuthenticationPrincipal User user) {
        List<MessageResponseDto> responses = messageService.getUserMessages(user.getId());
        return ResponseEntity.ok(responses);
    }
}
