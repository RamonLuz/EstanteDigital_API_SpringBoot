package com.estantedigital.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.estantedigital.exception.DomainRuleViolationException;

class DomainModelTest {

    @Test
    void bookRejectsInvalidPriceAndKeepsExistingDetails() {
        Book book = new Book("Clean Code", "Robert Martin", 100.0);

        DomainRuleViolationException exception = assertThrows(
                DomainRuleViolationException.class,
                () -> book.updateDetails("Updated", "Author", 0.0));

        assertEquals("price", exception.getField());
        assertEquals("Clean Code", book.getTitle());
        assertEquals(100.0, book.getPrice());
    }

    @Test
    void customerOwnsPurchaseRegistrationAndExposesReadOnlyPurchases() {
        Customer customer = new Customer("Ana", "12345678901", "ACTIVE");
        Book book = new Book("Clean Code", "Robert Martin", 100.0);

        customer.registerPurchase(book);
        customer.registerPurchase(book);

        assertEquals(1, customer.getPurchases().size());
        assertThrows(UnsupportedOperationException.class, () -> customer.getPurchases().clear());
    }

    @Test
    void customerRejectsInvalidIdentityWithoutChangingCurrentState() {
        Customer customer = new Customer("Ana", "12345678901", "ACTIVE");

        DomainRuleViolationException exception = assertThrows(
                DomainRuleViolationException.class,
                () -> customer.updateProfile("Ana Silva", "123", "INACTIVE"));

        assertEquals("cpf", exception.getField());
        assertEquals("Ana", customer.getName());
        assertEquals("12345678901", customer.getCpf());
        assertEquals("ACTIVE", customer.getStatus());
    }

    @Test
    void employeeRejectsInvalidRegistrationAndPreservesCurrentDetails() {
        Employee employee = new Employee("Joao", "12345678902", 1001, "Seller", "ACTIVE");

        DomainRuleViolationException exception = assertThrows(
                DomainRuleViolationException.class,
                () -> employee.updateDetails("Joao Silva", "12345678902", 0, "Manager", "INACTIVE"));

        assertEquals("registrationNumber", exception.getField());
        assertEquals("Joao", employee.getName());
        assertEquals(1001, employee.getRegistrationNumber());
        assertEquals("Seller", employee.getPosition());
    }

    @Test
    void saleRequiresAllParticipantsAndUsesProvidedDate() {
        Customer customer = new Customer("Ana", "12345678901", "ACTIVE");
        Employee employee = new Employee("Joao", "12345678902", 1001, "Seller", "ACTIVE");
        Book book = new Book("Clean Code", "Robert Martin", 100.0);
        LocalDate saleDate = LocalDate.of(2026, 11, 15);

        Sale sale = new Sale(customer, employee, book, saleDate);

        assertEquals(customer, sale.getCustomer());
        assertEquals(employee, sale.getEmployee());
        assertEquals(book, sale.getBook());
        assertEquals(saleDate, sale.getSaleDate());
    }
}
