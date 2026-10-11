package com.estantedigital.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.estantedigital.dto.BookRequestDTO;
import com.estantedigital.entity.Book;

class BookMapperTest {

    @Test
    void shouldMapRequestDtoToEntity() {
        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Clean Code");
        dto.setAuthor("Robert Martin");
        dto.setPrice(120.0);

        Book book = BookMapper.toEntity(dto);

        assertNotNull(book);
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals(120.0, book.getPrice());
    }

    @Test
    void shouldMapEntityToResponseDto() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setPrice(150.0);

        var response = BookMapper.toResponse(book);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Clean Code", response.getTitle());
        assertEquals("Robert Martin", response.getAuthor());
        assertEquals(150.0, response.getPrice());
    }
}
