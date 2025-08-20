// src/main/java/com/hashedin/huspark/controller/BookController.java
package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.BookDtos.BookRequest;
import com.hashedin.huspark.dto.BookDtos.BookResponse;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // Public read (also permitted in SecurityConfig)
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BookStatus status,
            @PageableDefault(size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.list(q, status, pageable));
    }

    // Public read
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    // Quick "scan" by code (ISBN/barcode)
    @GetMapping("/scan/{code}")
    public ResponseEntity<BookResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(bookService.getByCode(code));
    }

    // Create: LIBRARIAN or ADMIN
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody @Valid BookRequest req) {
        return ResponseEntity.ok(bookService.create(req));
    }

    // Update: LIBRARIAN or ADMIN
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id, @RequestBody @Valid BookRequest req) {
        return ResponseEntity.ok(bookService.update(id, req));
    }

    // Delete: ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.ok().build();
    }
}
