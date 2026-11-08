package com.estantedigital.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.estantedigital.entity.Book;
import com.estantedigital.exception.ApiException;
import com.estantedigital.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Page<Book> search(String title, String author, Pageable pageable) {
        return bookRepository.search(normalize(title), normalize(author), pageable);
    }

    public Book save(Book book) {
        if (book == null) {
            throw new ApiException(400, "Book is invalid", "Body");
        }

        if (book.getPrice() == null || book.getPrice() <= 0) {
            throw new ApiException(400, "Price is invalid", "Body");
        }

        if (bookRepository.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())) {
            throw new ApiException(400, "Book already exists for this author and title", "Body");
        }

        return bookRepository.save(book);
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> updateById(Long id, Book updatedBook) {
        Optional<Book> existingBook = bookRepository.findById(id);

        if (existingBook.isPresent()) {
            Book book = existingBook.get();
            book.setTitle(updatedBook.getTitle());
            book.setAuthor(updatedBook.getAuthor());
            book.setPrice(updatedBook.getPrice());

            bookRepository.save(book);
            return Optional.of(book);
        }

        return Optional.empty();
    }

    public boolean deleteById(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}