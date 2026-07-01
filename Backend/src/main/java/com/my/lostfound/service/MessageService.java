package com.my.lostfound.service;

import com.my.lostfound.dto.MessageRequestDto;
import com.my.lostfound.dto.MessageResponseDto;
import com.my.lostfound.entity.Item;
import com.my.lostfound.entity.Message;
import com.my.lostfound.entity.User;
import com.my.lostfound.exception.ResourceNotFoundException;
import com.my.lostfound.repository.ItemRepository;
import com.my.lostfound.repository.MessageRepository;
import com.my.lostfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.my.lostfound.exception.UnauthorizedException;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User not authenticated");
        }
        return ((User) auth.getPrincipal()).getId();
    }

    @Transactional
    public MessageResponseDto sendMessage(MessageRequestDto request) {
        Long currentUserId = getCurrentUserId();
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .item(item)
                .content(request.getContent())
                .build();

        Message savedMessage = messageRepository.save(message);

        return mapToResponseDto(savedMessage);
    }


    public List<MessageResponseDto> getUserMessages(Long userId) {
        if (!getCurrentUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to view these messages");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Message> messages = messageRepository.findBySenderIdOrReceiverIdOrderByCreatedAtAsc(userId, userId);
        
        return messages.stream()
                .map(this::mapToResponseDto)
                .toList();
    }


    private MessageResponseDto mapToResponseDto(Message message) {
        return MessageResponseDto.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getName())
                .itemId(message.getItem().getId())
                .itemTitle(message.getItem().getTitle())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
