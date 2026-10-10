package com.livrotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livrotech.entity.Book;
import com.livrotech.entity.ApiException;
import com.livrotech.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldListBooks() {
        Book book = newBook("Clean Code", "Robert Martin", 100.0);

        when(bookRepository.findAll())
                .thenReturn(List.of(book));

        List<Book> result = bookService.listAll();

        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
    }

    @Test
    void shouldSaveValidBook() {
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        when(bookRepository.findAll()).thenReturn(List.of());
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.save(book);

        assertSame(book, result);
        verify(bookRepository).save(book);
    }

    @Test
    void shouldRejectNullBook() {
        when(bookRepository.findAll()).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> bookService.save(null));

        assertEquals(400, exception.getStatus());
        assertEquals("Book is invalid", exception.getMessage());
        assertEquals("Body", exception.getField());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectDuplicateBookByTitleAndAuthor() {
        Book existingBook = newBook("Clean Code", "Robert Martin", 100.0);
        Book duplicateBook = newBook("Clean Code", "Robert Martin", 120.0);
        when(bookRepository.findAll()).thenReturn(List.of(existingBook));

        ApiException exception = assertThrows(ApiException.class, () -> bookService.save(duplicateBook));

        assertEquals("Book already exists for this author and title", exception.getMessage());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectBookWithoutPrice() {
        Book book = newBook("Clean Code", "Robert Martin", null);
        when(bookRepository.findAll()).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> bookService.save(book));

        assertEquals("Price is invalid", exception.getMessage());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectZeroOrNegativePrice() {
        when(bookRepository.findAll()).thenReturn(List.of());

        ApiException zeroPrice = assertThrows(ApiException.class,
                () -> bookService.save(newBook("Zero Price", "Author", 0.0)));
        ApiException negativePrice = assertThrows(ApiException.class,
                () -> bookService.save(newBook("Negative Price", "Author", -1.0)));

        assertEquals("Price is invalid", zeroPrice.getMessage());
        assertEquals("Price is invalid", negativePrice.getMessage());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldFindBookById() {
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(book, result.get());
    }

    @Test
    void shouldUpdateExistingBook() {
        Book existingBook = newBook("Old Title", "Old Author", 50.0);
        Book updatedBook = newBook("New Title", "New Author", 75.0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        Optional<Book> result = bookService.update(1L, updatedBook);

        assertTrue(result.isPresent());
        assertSame(existingBook, result.get());
        assertEquals("New Title", existingBook.getTitle());
        assertEquals("New Author", existingBook.getAuthor());
        assertEquals(75.0, existingBook.getPrice());
        verify(bookRepository).save(existingBook);
    }

    @Test
    void shouldNotUpdateMissingBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.update(1L, newBook("Title", "Author", 10.0));

        assertFalse(result.isPresent());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDeleteExistingBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        assertTrue(bookService.delete(1L));

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteMissingBook() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertFalse(bookService.delete(1L));

        verify(bookRepository, never()).deleteById(1L);
    }

    private Book newBook(String title, String author, Double price) {
        return new Book(null, title, author, price);
    }
}