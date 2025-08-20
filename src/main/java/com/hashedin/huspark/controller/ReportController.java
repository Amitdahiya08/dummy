// src/main/java/com/hashedin/huspark/controller/ReportController.java
package com.hashedin.huspark.controller;

import com.hashedin.huspark.entity.TransactionStatus;
import com.hashedin.huspark.repository.BorrowTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportController {

    private final BorrowTransactionRepository txRepo;

    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @GetMapping("/top-books")
    public ResponseEntity<?> topBooks() {
        var rows = txRepo.topBorrowed(PageRequest.of(0,3));
        var out = rows.stream().map(r -> Map.of(
                "title", (String) r[0],
                "code",  (String) r[1],
                "count", ((Number) r[2]).longValue()
        )).toList();
        return ResponseEntity.ok(out);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @GetMapping("/overdue-top")
    public ResponseEntity<?> overdueTop() {
        var list = txRepo.findByStatus(TransactionStatus.OVERDUE, PageRequest.of(0,3));
        var out = list.stream().map(t -> Map.of(
                "txId", t.getId(),
                "member", t.getMember().getEmail(),
                "title", t.getBook().getTitle(),
                "code",  t.getBook().getCode(),
                "dueAt", t.getDueAt()
        )).toList();
        return ResponseEntity.ok(out);
    }
}
