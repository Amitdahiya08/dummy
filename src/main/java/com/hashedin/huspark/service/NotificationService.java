// src/main/java/com/hashedin/huspark/service/NotificationService.java
package com.hashedin.huspark.service;

import com.hashedin.huspark.entity.*;
import com.hashedin.huspark.repository.BorrowTransactionRepository;
import com.hashedin.huspark.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final BorrowTransactionRepository txRepo;
    private final NotificationLogRepository logRepo;
    private final AuditService auditService;

    // Run every day at 9:00 AM (server time)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendOverdueNotifications() {
        var now = LocalDateTime.now();
        // find BORROWED transactions that are past due and mark as OVERDUE + notify
        txRepo.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.BORROWED && t.getDueAt().isBefore(now))
                .forEach(t -> {
                    t.setStatus(TransactionStatus.OVERDUE);
                    var msg = "Overdue: " + t.getBook().getTitle() + " (" + t.getBook().getCode() + ") was due on " + t.getDueAt();
                    logRepo.save(NotificationLog.builder()
                            .recipient(t.getMember().getEmail())
                            .channel("SMS")
                            .message(msg)
                            .sentAt(now)
                            .build());
                    auditService.log("NOTIFY_OVERDUE",
                            "{\"memberEmail\":\"" + t.getMember().getEmail() + "\",\"bookCode\":\"" + t.getBook().getCode() + "\"}", false);
                });
    }
}
