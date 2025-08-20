// src/main/java/com/hashedin/huspark/service/BookService.java
package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.BookDtos.BookRequest;
import com.hashedin.huspark.dto.BookDtos.BookResponse;
import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.repository.BookRepository;
import com.hashedin.huspark.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final AuditService auditService;

    private static BookResponse toDto(Book b) {
        return new BookResponse(
                b.getId(), b.getTitle(), b.getAuthor(),
                b.getCode(), b.getStatus(),
                b.getShelfRow(), b.getShelfCol()
        );
    }

    // Legacy convenience: non-paged list -> uses Pageable.unpaged() under the hood
    public List<BookResponse> list(String q, BookStatus status) {
        Page<Book> page;
        if (q != null && !q.isBlank()) {
            page = bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(q, q, Pageable.unpaged());
        } else if (status != null) {
            page = bookRepository.findByStatus(status, Pageable.unpaged());
        } else {
            page = bookRepository.findAll(Pageable.unpaged());
        }
        return page.map(BookService::toDto).toList();
    }

    public BookResponse getById(Long id) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        return toDto(b);
    }

    public BookResponse getByCode(String code) {
        Book b = bookRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Book not found"));
        return toDto(b);
    }

    public BookResponse create(BookRequest req) {
        // simple unique check on code
        bookRepository.findByCode(req.code()).ifPresent(b -> { throw new RuntimeException("Book code already exists"); });

        Book b = Book.builder()
                .title(req.title())
                .author(req.author())
                .code(req.code())
                .status(req.status() == null ? BookStatus.AVAILABLE : req.status())
                .shelfRow(req.shelfRow())
                .shelfCol(req.shelfCol())
                .build();

        // save first, then audit (so we have ID)
        Book saved = bookRepository.save(b);
        auditService.log("BOOK_CREATE", "{\"bookId\":" + saved.getId() + ",\"code\":\"" + saved.getCode() + "\"}", false);
        return toDto(saved);
    }

    public BookResponse update(Long id, BookRequest req) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));

        // if code is changing, ensure unique
        if (!b.getCode().equals(req.code())) {
            bookRepository.findByCode(req.code()).ifPresent(x -> { throw new RuntimeException("Book code already exists"); });
            b.setCode(req.code());
        }
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setStatus(req.status() == null ? b.getStatus() : req.status());
        b.setShelfRow(req.shelfRow());
        b.setShelfCol(req.shelfCol());

        Book saved = bookRepository.save(b);
        auditService.log("BOOK_UPDATE", "{\"bookId\":" + saved.getId() + "}", false);
        return toDto(saved);
    }

    // Paged list (for Milestone 4.1)
    public Page<BookResponse> list(String q, BookStatus status, Pageable pageable) {
        Page<Book> page;
        if (q != null && !q.isBlank()) {
            page = bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(q, q, pageable);
        } else if (status != null) {
            page = bookRepository.findByStatus(status, pageable);
        } else {
            page = bookRepository.findAll(pageable);
        }
        return page.map(BookService::toDto);
    }

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) throw new RuntimeException("Book not found");
        // audit before delete while we still know the id
        auditService.log("BOOK_DELETE", "{\"bookId\":" + id + "}", false);
        bookRepository.deleteById(id);
    }
}
