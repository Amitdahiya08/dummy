// src/main/java/com/hashedin/huspark/service/AuditService.java
package com.hashedin.huspark.service;

import com.hashedin.huspark.entity.AuditLog;
import com.hashedin.huspark.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {
    private final com.hashedin.huspark.repository.AuditLogRepository repo;

    public void log(String action, String detailsJson, boolean sensitive) {
        String actor = AuthUtil.currentEmail(); // may be null for system jobs
        AuditLog log = AuditLog.builder()
                .actorEmail(actor)
                .action(action)
                .detailsJson(sensitive ? mask(detailsJson) : detailsJson)
                .sensitive(sensitive)
                .createdAt(LocalDateTime.now())
                .build();
        repo.save(log);
    }

    // simplest “protection”: mask inside JSON string (keeps requirement minimal)
    private String mask(String s) {
        if (s == null) return null;
        // naive: replace digits and @-local parts
        return s.replaceAll("[0-9]", "x").replaceAll("([A-Za-z])(?=[A-Za-z._%+-]*@)", "*");
    }
}
