package com.my.lostfound.controller;

import com.my.lostfound.dto.NotificationResponseDto;
import com.my.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.my.lostfound.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(@AuthenticationPrincipal User user) {
        log.info("Fetching notifications for user id: {}", user.getId());
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        log.info("Marking notification id: {} as read for user id: {}", id, user.getId());
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        log.info("Marking all notifications as read for user id: {}", user.getId());
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
