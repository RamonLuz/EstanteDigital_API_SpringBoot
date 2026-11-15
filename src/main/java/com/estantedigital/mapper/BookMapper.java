package com.estantedigital.mapper;

import com.estantedigital.dto.BookRequestDTO;
import com.estantedigital.dto.BookResponseDTO;
import com.estantedigital.entity.Book;

public final class BookMapper {

    private BookMapper() {
    }

    public static Book toEntity(BookRequestDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPrice(dto.getPrice());
        return book;
    }

    public static BookResponseDTO toResponse(Book entity) {
        if (entity == null) {
            return null;
        }

        return new BookResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getPrice());
    }

}
