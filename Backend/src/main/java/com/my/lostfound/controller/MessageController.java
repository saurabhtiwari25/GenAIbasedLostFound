package com.my.lostfound.controller;

import com.my.lostfound.dto.MessageRequestDto;
import com.my.lostfound.dto.MessageResponseDto;
import com.my.lostfound.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@CrossOrigin("*") // Adjust in production
public class MessageController {

    private final MessageService messageService;


    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(@Valid @RequestBody MessageRequestDto request) {
        MessageResponseDto response = messageService.sendMessage(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MessageResponseDto>> getUserMessages(@PathVariable Long userId) {
        List<MessageResponseDto> responses = messageService.getUserMessages(userId);
        return ResponseEntity.ok(responses);
    }
}
