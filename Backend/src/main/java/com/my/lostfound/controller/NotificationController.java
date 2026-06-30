package com.my.lostfound.controller;

import com.my.lostfound.dto.NotificationResponseDto;
import com.my.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> getUserNotifications(@PathVariable Long userId) {
        log.info("Fetching notifications for user id: {}", userId);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }


    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        log.info("Marking notification id: {} as read for user id: {}", id, userId);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        log.info("Marking all notifications as read for user id: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
