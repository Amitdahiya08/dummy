// src/main/java/com/hashedin/huspark/entity/BorrowTransaction.java
package com.hashedin.huspark.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "borrow_transactions",
        indexes = {
                @Index(name = "idx_bt_book", columnList = "book_id"),
                @Index(name = "idx_bt_member", columnList = "member_id"),
                @Index(name = "idx_bt_status", columnList = "status")
        })
public class BorrowTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who borrowed
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bt_member"))
    private User member;

    // Which book
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bt_book"))
    private Book book;

    @NotNull
    private LocalDateTime borrowedAt;

    @NotNull
    private LocalDateTime dueAt;

    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.BORROWED;

    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;
}
