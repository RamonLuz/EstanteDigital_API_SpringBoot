package com.estantedigital.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.estantedigital.exception.DomainRuleViolationException;

@Entity
@Table(name = "books", uniqueConstraints = @UniqueConstraint(
        name = "uk_books_title_author",
        columnNames = { "title", "author" }))
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private Double price;

    protected Book() {
    }

    public Book(String title, String author, Double price) {
        updateDetails(title, author, price);
    }

    public Book(Long id, String title, String author, Double price) {
        this.id = id;
        updateDetails(title, author, price);
    }

    public void updateDetails(String title, String author, Double price) {
        if (title == null || title.isBlank()) {
            throw new DomainRuleViolationException("title", "Title is required");
        }
        if (author == null || author.isBlank()) {
            throw new DomainRuleViolationException("author", "Author is required");
        }
        if (price == null || price <= 0) {
            throw new DomainRuleViolationException("price", "Price must be greater than zero");
        }

        this.title = title.trim();
        this.author = author.trim();
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Double getPrice() {
        return price;
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Book other = (Book) obj;
        return Objects.equals(author, other.author) && Objects.equals(title, other.title);
    }
}