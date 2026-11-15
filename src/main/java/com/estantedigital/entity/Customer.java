package com.estantedigital.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.estantedigital.exception.DomainRuleViolationException;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(
        name = "uk_customers_cpf",
        columnNames = "cpf"))
public class Customer extends Person {

    @Column(nullable = false)
    private String status;

    @OneToMany
    private List<Book> purchases = new ArrayList<>();

    public Customer(String name, String cpf, String status, List<Book> purchases) {
        super(name, cpf);
        updateStatus(status);
        if (purchases != null) {
            this.purchases.addAll(purchases);
        }
    }

    public Customer(String name, String cpf, String status) {
        this(name, cpf, status, null);
    }

    protected Customer() {
    }

    public void updateProfile(String name, String cpf, String status) {
        validateStatus(status);
        changeIdentity(name, cpf);
        this.status = status;
    }

    public void updateStatus(String status) {
        validateStatus(status);
        this.status = status;
    }

    public void registerPurchase(Book book) {
        if (book == null) {
            throw new DomainRuleViolationException("book", "A book is required");
        }
        if (!purchases.contains(book)) {
            purchases.add(book);
        }
    }

    private void validateStatus(String status) {
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new DomainRuleViolationException("status", "Status must be ACTIVE or INACTIVE");
        }
    }

    public String getStatus() {
        return status;
    }

    public List<Book> getPurchases() {
        return List.copyOf(purchases);
    }
}
