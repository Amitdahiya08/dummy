package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BorrowTransactionRepositoryTest {

    @Autowired private UserRepository userRepo;
    @Autowired private BookRepository bookRepo;
    @Autowired private BorrowTransactionRepository txRepo;

    @Test
    void findByBookAndStatus_and_countByMemberAndStatus_work() {
        // user
        User u = User.builder()
                .fullName("Test Member")
                .email("member@test.com")
                .password("{noop}pwd") // not used here
                .role(Role.MEMBER)
                .build();
        u = userRepo.save(u);

        // book
        Book book = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .code("9780132350884")
                .status(BookStatus.AVAILABLE)
                .build();
        book = bookRepo.save(book);

        // tx: BORROWED
        BorrowTransaction tx = BorrowTransaction.builder()
                .member(u)
                .book(book)
                .borrowedAt(LocalDateTime.now())
                .dueAt(LocalDateTime.now().plusDays(14))
                .status(TransactionStatus.BORROWED)
                .lateFee(BigDecimal.ZERO)
                .build();
        txRepo.save(tx);

        // assert
        assertThat(txRepo.findByBookAndStatus(book, TransactionStatus.BORROWED)).isPresent();
        assertThat(txRepo.countByMemberAndStatus(u, TransactionStatus.BORROWED)).isEqualTo(1);
    }
}
