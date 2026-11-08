package com.estantedigital.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.estantedigital.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitle(String title);

    List<Book> findByAuthor(String author);

    List<Book> findByTitleAndAuthor(String title, String author);

    boolean existsByTitleAndAuthor(String title, String author);

    @Query("""
            select b from Book b
            where (:title is null or lower(b.title) like lower(concat('%', :title, '%')))
              and (:author is null or lower(b.author) like lower(concat('%', :author, '%')))
            """)
    Page<Book> search(@Param("title") String title, @Param("author") String author, Pageable pageable);
}