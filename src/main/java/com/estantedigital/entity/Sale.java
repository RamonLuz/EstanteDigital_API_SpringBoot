package com.estantedigital.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.estantedigital.exception.DomainRuleViolationException;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Customer customer;

    @ManyToOne(optional = false)
    private Employee employee;

    @ManyToOne(optional = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate saleDate = LocalDate.now();

    protected Sale() {
    }

    public Sale(Long id, Customer customer, Employee employee, Book book) {
        this.id = id;
        assignParticipants(customer, employee, book);
    }

    public Sale(Customer customer, Employee employee, Book book, LocalDate saleDate) {
        assignParticipants(customer, employee, book);
        this.saleDate = saleDate == null ? LocalDate.now() : saleDate;
    }

    private void assignParticipants(Customer customer, Employee employee, Book book) {
        if (customer == null || employee == null || book == null) {
            throw new DomainRuleViolationException("participants", "Customer, employee, and book are required");
        }
        this.customer = customer;
        this.employee = employee;
        this.book = book;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

}
