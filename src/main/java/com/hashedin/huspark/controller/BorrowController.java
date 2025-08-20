// src/main/java/com/hashedin/huspark/controller/BorrowController.java
package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.BorrowDtos.*;
import com.hashedin.huspark.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // --- Members: borrow/return their own books ---
    @PreAuthorize("hasAnyRole('MEMBER','LIBRARIAN','ADMIN')")
    @PostMapping
    public ResponseEntity<TransactionResponse> borrow(@RequestBody @Valid BorrowRequest req) {
        return ResponseEntity.ok(borrowService.borrowByCode(req.bookCode()));
    }

    @PreAuthorize("hasAnyRole('MEMBER','LIBRARIAN','ADMIN')")
    @PostMapping("/return")
    public ResponseEntity<TransactionResponse> returnBook(@RequestBody @Valid ReturnRequest req) {
        return ResponseEntity.ok(borrowService.returnByCode(req.bookCode()));
    }

    @PreAuthorize("hasAnyRole('MEMBER','LIBRARIAN','ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<TransactionResponse>> myTransactions() {
        return ResponseEntity.ok(borrowService.myTransactions());
    }

    // --- Staff views: overdue & reminders & inactive members ---
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @GetMapping("/overdue")
    public ResponseEntity<List<TransactionResponse>> overdue() {
        return ResponseEntity.ok(borrowService.overdue());
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @GetMapping("/reminders")
    public ResponseEntity<List<ReminderItem>> reminders() {
        return ResponseEntity.ok(borrowService.reminderList());
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @GetMapping("/inactive")
    public ResponseEntity<List<InactiveMember>> inactiveMembers(
            @RequestParam(defaultValue = "60") long days) {
        return ResponseEntity.ok(borrowService.inactiveMembers(days));
    }
}
