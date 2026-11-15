package com.estantedigital.service;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estantedigital.entity.Book;
import com.estantedigital.exception.ApiException;
import com.estantedigital.repository.BookRepository;

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

        List<Book> result = bookService.findAll();

        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
    }

    @Test
    void shouldSearchBooksAndTrimFilters() {
        Pageable pageable = PageRequest.of(0, 5);
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        Page<Book> expected = new PageImpl<>(List.of(book), pageable, 1);
        when(bookRepository.search("clean", "martin", pageable)).thenReturn(expected);

        Page<Book> result = bookService.search(" clean ", "martin ", pageable);

        assertEquals(expected, result);
        verify(bookRepository).search("clean", "martin", pageable);
    }

    @Test
    void shouldSaveValidBook() {
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        when(bookRepository.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.save(book);

        assertSame(book, result);
        verify(bookRepository).save(book);
    }

    @Test
    void shouldRejectNullBook() {
        ApiException exception = assertThrows(ApiException.class, () -> bookService.save(null));

        assertEquals(400, exception.getStatus());
        assertEquals("Book is invalid", exception.getMessage());
        assertEquals("Body", exception.getField());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectDuplicateBookByTitleAndAuthor() {
        Book duplicateBook = newBook("Clean Code", "Robert Martin", 120.0);
        when(bookRepository.existsByTitleAndAuthor("Clean Code", "Robert Martin")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> bookService.save(duplicateBook));

        assertEquals("Book already exists for this author and title", exception.getMessage());
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

        Optional<Book> result = bookService.updateById(1L, updatedBook);

        assertTrue(result.isPresent());
        assertSame(existingBook, result.get());
        assertEquals("New Title", existingBook.getTitle());
        assertEquals("New Author", existingBook.getAuthor());
        assertEquals(75.0, existingBook.getPrice());
        verify(bookRepository).save(existingBook);
    }

    @Test
    void shouldRejectUpdatingBookToAnotherBooksTitleAndAuthor() {
        Book existingBook = newBook("Old Title", "Old Author", 50.0);
        Book updatedBook = newBook("Existing Title", "Existing Author", 75.0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByTitleAndAuthorAndIdNot("Existing Title", "Existing Author", 1L))
                .thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> bookService.updateById(1L, updatedBook));

        assertEquals(409, exception.getStatus());
        assertEquals("Book already exists for this author and title", exception.getMessage());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotUpdateMissingBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.updateById(1L, newBook("Title", "Author", 10.0));

        assertFalse(result.isPresent());
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDeleteExistingBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        assertTrue(bookService.deleteById(1L));

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteMissingBook() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertFalse(bookService.deleteById(1L));

        verify(bookRepository, never()).deleteById(1L);
    }

    private Book newBook(String title, String author, Double price) {
        return new Book(null, title, author, price);
    }
}