package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.BorrowDtos.InactiveMember;
import com.hashedin.huspark.dto.BorrowDtos.ReminderItem;
import com.hashedin.huspark.dto.BorrowDtos.TransactionResponse;
import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.entity.BorrowTransaction;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.TransactionStatus;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.BookRepository;
import com.hashedin.huspark.repository.BorrowTransactionRepository;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hashedin.huspark.service.AuditService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowService {

    private final BorrowTransactionRepository txRepo;
    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final AuditService auditService;

    // simple, tweakable rules
    private static final int MAX_ACTIVE_BORROWS = 5;     // cap per user
    private static final int LOAN_DAYS = 14;             // standard due window
    private static final int LATE_GRACE_DAYS = 30;       // free grace after due
    private static final BigDecimal LATE_FEE_PER_DAY = BigDecimal.valueOf(5); // per day after grace

    private User currentUser() {
        String email = AuthUtil.currentEmail();
        return userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private static TransactionResponse toDto(BorrowTransaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getBook().getId(),
                t.getBook().getTitle(),
                t.getBook().getCode(),
                t.getMember().getId(),
                t.getMember().getEmail(),
                t.getStatus().name(),
                t.getBorrowedAt(),
                t.getDueAt(),
                t.getReturnedAt(),
                t.getLateFee()
        );
    }

    // ========== Borrow ==========
    public TransactionResponse borrowByCode(String bookCode) {
        User member = currentUser();
        Book book = bookRepo.findByCode(bookCode).orElseThrow(() -> new RuntimeException("Book not found"));

        // Non-borrowable?
        if (book.getStatus() == BookStatus.NON_BORROWABLE) {
            throw new RuntimeException("This book is not borrowable");
        }

        // Already borrowed by someone?
        txRepo.findByBookAndStatus(book, TransactionStatus.BORROWED)
                .ifPresent(t -> { throw new RuntimeException("Book is already borrowed"); });

        // Prevent same member borrowing same book twice without returning
        txRepo.findByMemberAndBookAndStatus(member, book, TransactionStatus.BORROWED)
                .ifPresent(t -> { throw new RuntimeException("You already borrowed this book"); });

        // Max cap
        long activeCount = txRepo.countByMemberAndStatus(member, TransactionStatus.BORROWED);
        if (activeCount >= MAX_ACTIVE_BORROWS) {
            throw new RuntimeException("Max active borrows reached (" + MAX_ACTIVE_BORROWS + ")");
        }

        // OK to borrow
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = now.plusDays(LOAN_DAYS);
        BorrowTransaction tx = BorrowTransaction.builder()
                .member(member)
                .book(book)
                .borrowedAt(now)
                .dueAt(due)
                .status(TransactionStatus.BORROWED)
                .lateFee(BigDecimal.ZERO)
                .build();

        // Update book status → BORROWED
        book.setStatus(BookStatus.BORROWED);
        bookRepo.save(book);

        // Save transaction to get ID, then audit
        BorrowTransaction saved = txRepo.save(tx);
        auditService.log("BORROW", "{\"txId\":" + saved.getId() + ",\"bookCode\":\"" + bookCode + "\"}", false);

        return toDto(saved);
    }

    // ========== Return ==========
    public TransactionResponse returnByCode(String bookCode) {
        User member = currentUser();
        Book book = bookRepo.findByCode(bookCode).orElseThrow(() -> new RuntimeException("Book not found"));

        // Must have an active BORROWED tx for this member+book
        BorrowTransaction tx = txRepo.findByMemberAndBookAndStatus(member, book, TransactionStatus.BORROWED)
                .orElseThrow(() -> new RuntimeException("No active borrowing record for this book"));

        LocalDateTime now = LocalDateTime.now();
        tx.setReturnedAt(now);

        // Overdue check
        if (now.isAfter(tx.getDueAt())) {
            tx.setStatus(TransactionStatus.OVERDUE);
            // Late fee only after 30 days beyond due date (simple rule)
            long daysLate = ChronoUnit.DAYS.between(tx.getDueAt(), now);
            long feeDays = Math.max(0, daysLate - LATE_GRACE_DAYS);
            if (feeDays > 0) {
                tx.setLateFee(LATE_FEE_PER_DAY.multiply(BigDecimal.valueOf(feeDays)));
            }
        } else {
            tx.setStatus(TransactionStatus.RETURNED);
        }

        // Book becomes AVAILABLE again
        book.setStatus(BookStatus.AVAILABLE);
        bookRepo.save(book);

        // Save transaction to get final state, then audit
        BorrowTransaction saved = txRepo.save(tx);
        auditService.log("RETURN", "{\"txId\":" + saved.getId() + ",\"bookCode\":\"" + bookCode + "\"}", false);

        return toDto(saved);
    }

    // ========== Queries ==========

    // Member: my transactions (active first)
    @Transactional(readOnly = true)
    public List<TransactionResponse> myTransactions() {
        User me = currentUser();
        return txRepo.findByMember(me).stream()
                .sorted((a, b) -> {
                    // sort active BORROWED first, then dueAt soonest
                    if (a.getStatus() != b.getStatus()) {
                        return a.getStatus() == TransactionStatus.BORROWED ? -1 : 1;
                    }
                    return a.getDueAt().compareTo(b.getDueAt());
                })
                .map(BorrowService::toDto)
                .toList();
    }

    // Staff: list overdue
    @Transactional(readOnly = true)
    public List<TransactionResponse> overdue() {
        return txRepo.findByStatus(TransactionStatus.OVERDUE, Pageable.unpaged())
                .stream()
                .map(BorrowService::toDto)
                .toList();
    }

    // Staff: reminders for due soon (within next 2 days) or already overdue
    @Transactional(readOnly = true)
    public List<ReminderItem> reminderList() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soon = now.plusDays(2);

        return txRepo.findByStatus(TransactionStatus.BORROWED, Pageable.unpaged())
                .stream()
                .filter(t -> !t.getDueAt().isAfter(soon)) // dueAt <= soon
                .map(t -> new ReminderItem(
                        t.getMember().getEmail(),
                        t.getBook().getTitle(),
                        t.getBook().getCode(),
                        t.getDueAt()
                ))
                .toList();
    }

    // Staff: inactive members who haven't borrowed in N days (default 60)
    @Transactional(readOnly = true)
    public List<InactiveMember> inactiveMembers(long days) {
        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.MEMBER) // focus on members
                .map(u -> {
                    var list = txRepo.findByMember(u);
                    LocalDateTime last = list.stream()
                            .map(BorrowTransaction::getBorrowedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(LocalDateTime.MIN);
                    long gap = (last.equals(LocalDateTime.MIN))
                            ? days + 1 // never borrowed
                            : ChronoUnit.DAYS.between(last, LocalDateTime.now());
                    return new InactiveMember(u.getId(), u.getEmail(), gap);
                })
                .filter(im -> im.daysSinceLastBorrow() >= days)
                .sorted((a, b) -> Long.compare(b.daysSinceLastBorrow(), a.daysSinceLastBorrow()))
                .toList();
    }
}
