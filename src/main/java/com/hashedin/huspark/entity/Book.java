// src/main/java/com/hashedin/huspark/entity/Book.java
package com.hashedin.huspark.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "books", uniqueConstraints = {
        @UniqueConstraint(name = "uk_books_code", columnNames = "code")
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 200)
    private String title;

    @NotBlank @Size(max = 160)
    private String author;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false)
    private String code; // single unique identifier (ISBN or library barcode)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookStatus status = BookStatus.AVAILABLE;

    // Simple location for reshelving
    private Integer shelfRow;
    private Integer shelfCol;
}
