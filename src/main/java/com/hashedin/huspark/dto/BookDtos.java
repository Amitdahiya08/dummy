// src/main/java/com/hashedin/huspark/dto/BookDtos.java
package com.hashedin.huspark.dto;

import com.hashedin.huspark.entity.BookStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookDtos {

    public record BookRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 160) String author,
            @NotBlank @Size(max = 64)  String code,          // unique (ISBN or barcode)
            BookStatus status,                                // optional; defaults to AVAILABLE
            Integer shelfRow,
            Integer shelfCol
    ) {}

    public record BookResponse(
            Long id,
            String title,
            String author,
            String code,
            BookStatus status,
            Integer shelfRow,
            Integer shelfCol
    ) {}
}
