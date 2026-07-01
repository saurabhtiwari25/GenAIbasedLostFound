package com.my.lostfound.service;

import com.my.lostfound.dto.CommentRequestDto;
import com.my.lostfound.dto.CommentResponseDto;
import com.my.lostfound.entity.Comment;
import com.my.lostfound.entity.Item;
import com.my.lostfound.entity.User;
import com.my.lostfound.exception.ResourceNotFoundException;
import com.my.lostfound.repository.CommentRepository;
import com.my.lostfound.repository.ItemRepository;
import com.my.lostfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.my.lostfound.exception.UnauthorizedException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User not authenticated");
        }
        return ((User) auth.getPrincipal()).getId();
    }

    public CommentResponseDto addComment(Long itemId, CommentRequestDto dto) {
        Long currentUserId = getCurrentUserId();
        log.info("Adding comment to item id: {} by user id: {}", itemId, currentUserId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", itemId);
                    return new ResourceNotFoundException("Item not found");
                });

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .item(item)
                .author(author)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Successfully added comment with id: {}", savedComment.getId());

        return mapToDto(savedComment);
    }


    public List<CommentResponseDto> getCommentsByItemId(Long itemId) {
        log.info("Fetching all comments for item id: {}", itemId);
        return commentRepository.findByItemIdOrderByCreatedAtAsc(itemId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    private CommentResponseDto mapToDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
