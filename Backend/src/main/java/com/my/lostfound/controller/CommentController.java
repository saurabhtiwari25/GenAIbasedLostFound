package com.my.lostfound.controller;

import com.my.lostfound.dto.CommentRequestDto;
import com.my.lostfound.dto.CommentResponseDto;
import com.my.lostfound.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/item/{itemId}")
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto dto) {
        log.info("Adding comment to item id: {}", itemId);
        CommentResponseDto response = commentService.addComment(itemId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByItemId(@PathVariable Long itemId) {
        log.info("Fetching comments for item id: {}", itemId);
        List<CommentResponseDto> response = commentService.getCommentsByItemId(itemId);
        return ResponseEntity.ok(response);
    }
}
