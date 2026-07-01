package com.my.lostfound.service;

import com.my.lostfound.dto.NotificationResponseDto;
import com.my.lostfound.entity.Notification;
import com.my.lostfound.entity.User;
import com.my.lostfound.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.my.lostfound.exception.ResourceNotFoundException;
import com.my.lostfound.exception.UnauthorizedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User not authenticated");
        }
        return ((User) auth.getPrincipal()).getId();
    }


    @Transactional
    public void createNotification(User user, String message) {
        log.info("Creating notification for user id: {}", user.getId());
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }


    public List<NotificationResponseDto> getUserNotifications(Long userId) {
        if (!getCurrentUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to view these notifications");
        }
        log.info("Fetching notifications for user id: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification id: {} as read for user id: {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Notification not found with id: {}", notificationId);
                    return new ResourceNotFoundException("Notification not found");
                });
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }


    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user id: {}", userId);
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }


    private NotificationResponseDto mapToDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
