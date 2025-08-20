// src/main/java/com/hashedin/huspark/controller/AuditController.java
package com.hashedin.huspark.controller;

import com.hashedin.huspark.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/audit") @RequiredArgsConstructor
public class AuditController {
    private final AuditLogRepository repo;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(repo.findAll()); }
}
