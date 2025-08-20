package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.BorrowTransaction;
import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.TransactionStatus;
import com.hashedin.huspark.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    @Query("select t.book.title, t.book.code, count(t) as c from BorrowTransaction t group by t.book.title, t.book.code order by c desc")
    List<Object[]> topBorrowed(Pageable pageable);
    List<BorrowTransaction> findByStatus(TransactionStatus status, Pageable pageable);

    List<BorrowTransaction> findByMember(User member);
    List<BorrowTransaction> findByBook(Book book);
//    List<BorrowTransaction> findByStatus(TransactionStatus status);

    Optional<BorrowTransaction> findByBookAndStatus(Book book, TransactionStatus status);
    Optional<BorrowTransaction> findByMemberAndBookAndStatus(User member, Book book, TransactionStatus status);

    long countByMemberAndStatus(User member, TransactionStatus status);
    List<BorrowTransaction> findByDueAtBeforeAndStatus(LocalDateTime time, TransactionStatus status);
    List<BorrowTransaction> findByBorrowedAtAfter(LocalDateTime time);
    List<BorrowTransaction> findByMemberAndStatus(User member, TransactionStatus status);


}
