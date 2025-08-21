package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.BookDtos.BookRequest;
import com.hashedin.huspark.dto.BookDtos.BookResponse;
import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuditService auditService;

    @InjectMocks private BookService bookService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_saves_book_and_returns_dto() {
        BookRequest req = new BookRequest(
                "Clean Code", "Robert C. Martin", "9780132350884",
                null, 1, 2
        );

        when(bookRepository.findByCode("9780132350884")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookResponse resp = bookService.create(req);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.title()).isEqualTo("Clean Code");
        assertThat(resp.status()).isEqualTo(BookStatus.AVAILABLE);
        verify(auditService).log(eq("BOOK_CREATE"), contains("\"bookId\":1"), eq(false));
    }

    @Test
    void list_with_query_uses_search_repo() {
        Book b = Book.builder().id(1L).title("Clean Code").author("Uncle Bob").code("978").status(BookStatus.AVAILABLE).build();
        Page<Book> page = new PageImpl<>(java.util.List.of(b));

        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(eq("clean"), eq("clean"), any(Pageable.class)))
                .thenReturn(page);

        var out = bookService.list("clean", null, PageRequest.of(0, 10));
        assertThat(out.getContent()).hasSize(1);
        assertThat(out.getContent().get(0).title()).isEqualTo("Clean Code");
    }
}
