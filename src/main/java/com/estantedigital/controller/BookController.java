package com.estantedigital.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.estantedigital.dto.BookRequestDTO;
import com.estantedigital.dto.BookResponseDTO;
import com.estantedigital.dto.PageResponseDTO;
import com.estantedigital.entity.Book;
import com.estantedigital.mapper.BookMapper;
import com.estantedigital.service.BookService;
import com.estantedigital.util.Pagination;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<BookResponseDTO>> getAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = Pagination.request(page, size);
        Page<BookResponseDTO> result = bookService.search(title, author, pageable).map(BookMapper::toResponse);
        return ResponseEntity.ok(PageResponseDTO.from(result));
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> create(@Valid @RequestBody BookRequestDTO dto) {
        Book book = BookMapper.toEntity(dto);

        Book savedBook = bookService.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookMapper.toResponse(savedBook));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getById(@PathVariable Long id) {
        Optional<Book> book = bookService.findById(id);

        if (book.isPresent()) {
            return ResponseEntity.ok(BookMapper.toResponse(book.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> update(@PathVariable Long id, @Valid @RequestBody BookRequestDTO dto) {
        Book book = BookMapper.toEntity(dto);

        Optional<Book> updatedBook = bookService.updateById(id, book);

        if (updatedBook.isPresent()) {
            return ResponseEntity.ok(BookMapper.toResponse(updatedBook.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = bookService.deleteById(id);

        if (removed) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}