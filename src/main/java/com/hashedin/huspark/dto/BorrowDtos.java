// src/main/java/com/hashedin/huspark/dto/BorrowDtos.java
package com.hashedin.huspark.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BorrowDtos {

    // Borrow/return by book code (ISBN/barcode)
    public record BorrowRequest(String bookCode) {}
    public record ReturnRequest(String bookCode) {}

    public record TransactionResponse(
            Long id,
            Long bookId,
            String bookTitle,
            String bookCode,
            Long memberId,
            String memberEmail,
            String status,
            LocalDateTime borrowedAt,
            LocalDateTime dueAt,
            LocalDateTime returnedAt,
            BigDecimal lateFee
    ) {}

    public record ReminderItem(
            String memberEmail,
            String bookTitle,
            String bookCode,
            LocalDateTime dueAt
    ) {}

    public record InactiveMember(
            Long memberId,
            String email,
            long daysSinceLastBorrow
    ) {}
}
