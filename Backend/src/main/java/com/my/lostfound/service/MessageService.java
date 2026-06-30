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

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Transactional
    public MessageResponseDto sendMessage(MessageRequestDto request) {
        User sender = userRepository.findById(request.getSenderId())
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
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Message> messages = messageRepository.findBySenderIdOrReceiverIdOrderByCreatedAtAsc(userId, userId);
        
        return messages.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
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
