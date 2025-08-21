package com.hashedin.huspark.controller;

import com.hashedin.huspark.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/trigger")
    public ResponseEntity<?> triggerNotifications() {
        notificationService.sendOverdueNotifications();
        return ResponseEntity.ok("📩 Overdue notifications has been sent");
    }
}
